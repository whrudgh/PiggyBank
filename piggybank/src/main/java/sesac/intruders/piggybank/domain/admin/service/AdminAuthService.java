package sesac.intruders.piggybank.domain.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import sesac.intruders.piggybank.domain.admin.dto.request.AdminChangePasswordDTO;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminLoginDTO;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminResetPasswordDTO;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminVerifyDTO;
import sesac.intruders.piggybank.domain.admin.model.Admin;
import sesac.intruders.piggybank.domain.admin.repository.AdminRepository;
import sesac.intruders.piggybank.global.security.jwt.JwtTokenProvider;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AdminAuthService {
    private static final String REDIS_VERIFICATION_PREFIX = "admin:pwd:verify:";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int VERIFICATION_TOKEN_LENGTH = 32;
    private static final int TEMP_PASSWORD_LENGTH = 12;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    public String login(AdminLoginDTO loginDTO) {
        Admin admin = adminRepository.findByAdminId(loginDTO.getAdminId())
                .orElseThrow(() -> new RuntimeException("Invalid admin credentials"));

        // BCrypt를 사용하여 비밀번호 검증
        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid admin credentials");
        }

        // 관리자 전용 토큰 생성
        return jwtTokenProvider.generateAdminToken(admin.getAdminId(), "ROLE_ADMIN");
    }

    public void logout(String token) {
        long expiration = jwtTokenProvider.getExpiration(token);

        // Redis에 블랙리스트 등록
        redisTemplate.opsForValue().set(token, "logged-out", expiration);
    }

    public String verifyAdminInfo(AdminVerifyDTO dto) {
        log.info("Verifying admin info for adminId: {}", dto.getAdminId());
        log.info("Name: {}, Email: {}, Phone: {}", dto.getName(), dto.getEmail(), dto.getPhoneNumber());

        // 엄격한 검증 - adminId, name, email, phoneNumber 모두 DB값과 일치해야 함
        Admin admin = adminRepository.findByAdminId(dto.getAdminId())
                .orElseThrow(() -> {
                    log.error("Admin not found with id: {}", dto.getAdminId());
                    return new RuntimeException("사용자 인증 실패");
                });

        log.info("Found admin in DB - Name: {}, Email: {}, Phone: {}", 
            admin.getName(), admin.getEmail(), admin.getPhoneNumber());

        // 모든 필드 검증
        if (!admin.getName().equals(dto.getName()) ||
            !admin.getEmail().equals(dto.getEmail()) ||  
            !admin.getPhoneNumber().equals(dto.getPhoneNumber())) {
            log.error("Verification failed - fields don't match");
            log.error("DB values: name={}, email={}, phone={}", 
                admin.getName(), admin.getEmail(), admin.getPhoneNumber());
            log.error("Input values: name={}, email={}, phone={}", 
                dto.getName(), dto.getEmail(), dto.getPhoneNumber());
            throw new RuntimeException("사용자 인증 실패");
        }

        // 검증 성공 시 임시 토큰 생성
        String verificationToken = generateVerificationToken();
        
        // Redis에 검증 토큰 저장 (5분간 유효)
        String redisKey = REDIS_VERIFICATION_PREFIX + dto.getAdminId();
        redisTemplate.opsForValue().set(redisKey, verificationToken, 5, TimeUnit.MINUTES);

        return verificationToken;
    }

    public void resetPassword(String verificationToken, AdminResetPasswordDTO dto) {
        // Redis에서 검증 토큰 확인
        String redisKey = REDIS_VERIFICATION_PREFIX + dto.getAdminId();
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        
        if (storedToken == null || !storedToken.equals(verificationToken)) {
            throw new RuntimeException("유효하지 않거나 만료된 검증 토큰입니다.");
        }

        Admin admin = adminRepository.findByAdminId(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        // 임시 비밀번호 생성
        String tempPassword = generateTempPassword();
        
        // 비밀번호 암호화 및 저장
        admin.setPassword(passwordEncoder.encode(tempPassword));
        adminRepository.save(admin);

        // 이메일 전송
        sendTempPasswordEmail(dto.getEmail(), tempPassword);

        // 검증 토큰 삭제
        redisTemplate.delete(redisKey);
    }

    public void changePassword(AdminChangePasswordDTO dto) {
        log.info("Changing password for adminId: {}", dto.getAdminId());
        
        Admin admin = adminRepository.findByAdminId(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), admin.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        validateNewPassword(dto.getNewPassword(), dto.getCurrentPassword());

        // 새 비밀번호 암호화 및 저장
        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        admin.setPassword(encodedNewPassword);
        adminRepository.save(admin);
        
        log.info("Password changed successfully for adminId: {}", dto.getAdminId());
    }

    private void validateNewPassword(String newPassword, String currentPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("새 비밀번호는 최소 8자 이상이어야 합니다.");
        }
        
        if (newPassword.equals(currentPassword)) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 같을 수 없습니다.");
        }

        // 비밀번호 복잡성 검사 - 소문자와 숫자는 필수, 대문자와 특수문자는 선택사항
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        
        for (char c : newPassword.toCharArray()) {
            if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        if (!(hasLowerCase && hasDigit)) {
            throw new RuntimeException("새 비밀번호는 소문자와 숫자를 포함해야 합니다. (대문자, 특수문자는 선택사항)");
        }
    }

    private String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < VERIFICATION_TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return token.toString();
    }

    private String generateTempPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return password.toString();
    }

    private void sendTempPasswordEmail(String email, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(email);
            helper.setSubject("[PiggyBank] 임시 비밀번호가 발급되었습니다.");
            helper.setText("임시 비밀번호: " + tempPassword + "\n\n로그인 후 반드시 비밀번호를 변경해주세요.", false);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }
}
