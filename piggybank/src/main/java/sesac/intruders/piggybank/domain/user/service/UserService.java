package sesac.intruders.piggybank.domain.user.service;

import static sesac.intruders.piggybank.global.common.response.SuccessCode.*;
import static sesac.intruders.piggybank.global.error.ErrorCode.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.Login;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.Register;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.RegisterCheck;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.RegisterCheckEmail;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.RegisterCheckId;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.Reissue;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.LoginFindId;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.RegisterDuplicateCheck;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.TokenInfo;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.UserInfo;
import sesac.intruders.piggybank.domain.user.model.Role;
import sesac.intruders.piggybank.domain.user.model.User;
import sesac.intruders.piggybank.domain.user.model.UserJob;
import sesac.intruders.piggybank.domain.user.model.UserTrsfLimit;
import sesac.intruders.piggybank.domain.user.repository.SmsCertification;
import sesac.intruders.piggybank.domain.user.repository.UserJobRepository;
import sesac.intruders.piggybank.domain.user.repository.UserRepository;
import sesac.intruders.piggybank.domain.user.repository.UserTrsfLimitRepository;
import sesac.intruders.piggybank.domain.user.util.SmsUtil;
import sesac.intruders.piggybank.global.common.response.ApiResponse;
import sesac.intruders.piggybank.global.common.response.SuccessCode;
import sesac.intruders.piggybank.global.error.ErrorCode;
import sesac.intruders.piggybank.global.error.exception.CustomException;
import sesac.intruders.piggybank.global.security.jwt.JwtTokenProvider;
import sesac.intruders.piggybank.domain.user.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
//@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserJobRepository userJobRepository;
    private final UserTrsfLimitRepository trsfLimitRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisTemplate redisTemplate;
    private final SmsUtil smsUtil;
    private final SmsCertification smsCertification;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND.getMessage()));
    }

    public ApiResponse<UserResponseDto.UserCodeResponse> getUserCode(String userId) {
        UUID userCode = userRepository.findUserCodeByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
        return ApiResponse.success(SuccessCode.USER_GET_INFO_SUCCESS, new UserResponseDto.UserCodeResponse(userCode));
    }

    // 회원가입 여부 확인
    public ApiResponse<RegisterDuplicateCheck> registerCheck(RegisterCheck request) {
        // return value. 계정이 이미 존재하면 true, 아니면 false 반환
        Boolean value = false;

        if (userRepository.existsByUserNameKrAndUserPhone(request.getUserNameKr(), request.getUserPhone())) {
            value = true;
        }

        return ApiResponse.success(USER_REGISTER_CHECK_SUCCESS, new RegisterDuplicateCheck(value));
    }

    // 중복 이메일 여부 확인
    public ApiResponse<RegisterDuplicateCheck> registerCheckEmail(RegisterCheckEmail request) {
        // return value. 중복이면 true, 아니면 false 반환
        Boolean value = false;

        if (userRepository.existsByUserEmail(request.getUserEmail())) {
            value = true;
        }

        return ApiResponse.success(USER_REGISTER_CHECK_EMAIL_SUCCESS, new RegisterDuplicateCheck(value));
    }

    // 중복 아이디 여부 확인
    public ApiResponse<RegisterDuplicateCheck> registerCheckId(RegisterCheckId request) {
        // return value. 중복이면 true, 아니면 false 반환
        Boolean value = false;

        if (userRepository.existsByUserId(request.getUserId())) {
            value = true;
        }

        return ApiResponse.success(USER_REGISTER_CHECK_ID_SUCCESS, new RegisterDuplicateCheck(value));
    }

    // 회원가입
    public ApiResponse<Object> register(Register register) {
        // RequestDto to Entity
        User user = User.builder()
                .userId(register.getUserId())
                .userPwd(passwordEncoder.encode(register.getUserPwd()))
                .userNameKr(register.getUserNameKr())
                .userInherentNumber(register.getUserInherentNumber() + "******")
                .userPhone(register.getUserPhone())
                .userAddr(register.getUserAddr())
                .status("ACTIVE")
                .userAddrDetail(register.getUserAddrDetail())
                .userNameEn(register.getUserNameEn())
                .userEmail(register.getUserEmail())
                .roles(Collections.singletonList(Role.ROLE_USER.name()))
                .build();

        userRepository.save(user);

        // UserJob 생성
        UserJob userJob = UserJob.builder()
                .userCode(user.getUserCode())
                .user(user)
                .build();

        userJobRepository.save(userJob);

        // UserTrsfLimit 생성
        UserTrsfLimit userTrsfLimit = UserTrsfLimit.builder()
                .user(user)
                .build();

        trsfLimitRepository.save(userTrsfLimit);

        return ApiResponse.success(USER_REGISTER_SUCCESS);
    }

    // 로그인
    public ApiResponse<TokenInfo> login(Login login, Role role) {
        // 로그인 정보 계정 조회
        User user = getUserByUserId(login.getUserId());

        log.info("role == Role.ROLE_ADMIN: {}", role == Role.ROLE_ADMIN);
        log.info("user.getRoles().contains(Role.ROLE_ADMIN.name()): {}", user.getRoles().contains(Role.ROLE_ADMIN.name()));

        // 관리자 계정 로그인 검증
        // 관리자 페이지에서 로그인 시도 계정이 관리자 계정이 아니면 실행
        if(role == Role.ROLE_ADMIN && !user.getRoles().contains(Role.ROLE_ADMIN.name())) {
            throw new CustomException(USER_ADMIN_LOGIN_FAIL);
        }

        // 1. 로그인 아이디, 비밀번호를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = login.toAuthentication();
        // authenticationToken 내에는 id, pwd 들어가 있다

        // 2. 실제 검증(사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 함수가 실행될 때 loadUserByUsername 함수 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // authenticationManagerBuilder -> id, pwd 일치 여부 판단
        // authentication -> 권한 정보 저장

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        log.info("RT: " + authentication.getName() + " : " + tokenInfo.getRefreshToken() + " : " + tokenInfo.getRefreshTokenExpirationTime() + " : " + TimeUnit.MILLISECONDS);

        // 4. RefreshToken Redis 저장 (expirationTime 설정을 통해 자동 삭제 처리)
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return ApiResponse.success(USER_LOGIN_SUCCESS, tokenInfo);
    }

    // 토큰 재발급
    public ApiResponse<TokenInfo> reissue(Reissue reissue) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(reissue.getRefreshToken())) {
            // message: "Refresh Token 정보가 유효하지 않습니다."
            throw new CustomException(USER_REISSUE_FAIL);
        }

        // 2. Access Token 에서 User ID 을 가져옵니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(reissue.getAccessToken());
        log.info("*** Access Token 저장 정보 조회 1. authentication.getName() = {}", authentication.getName());
//        log.info("*** Access Token 저장 정보 조회 2. authentication.getDetails().toString() = {}", authentication.getDetails().toString());

        // 3. Redis 에서 User ID 를 기반으로 저장된 Refresh Token 값을 가져옵니다.
        String refreshToken = (String) redisTemplate.opsForValue().get("RT:" + authentication.getName());

        // (추가) 로그아웃되어 Redis 에 RefreshToken 이 존재하지 않는 경우 처리
        if (ObjectUtils.isEmpty(refreshToken)) {
            // message: "잘못된 요청입니다."
            throw new CustomException(USER_REISSUE_FAIL);
        }

        if (!refreshToken.equals(reissue.getRefreshToken())) {
            // message: "Refresh Token 정보가 일치하지 않습니다."
            throw new CustomException(USER_REISSUE_FAIL);
        }

        // 4. 새로운 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 5. RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), tokenInfo.getRefreshToken(), tokenInfo.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        return ApiResponse.success(USER_REISSUE_SUCCESS, tokenInfo);
    }

    // 아이디 조회
    public ApiResponse<LoginFindId> loginFindId(UserRequestDto.LoginFindId request) {
        String userId = userRepository.findUserIdByUserNameKrAndUserEmail(request.getUserNameKr(), request.getUserEmail())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return ApiResponse.success(USER_FIND_ID_SUCCESS, new LoginFindId(userId));
    }

    // 로그아웃
    public ApiResponse<Object> logout(UserRequestDto.Logout logout) {
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(logout.getAccessToken())) {
            throw new CustomException(USER_LOGOUT_ACCESS_TOKEN_VALIDATION_FAIL);
        }

        // 2. Access Token 에서 User email 을 가져옵니다.
        Authentication authentication = jwtTokenProvider.getAuthentication(logout.getAccessToken());

        // 3. Redis 에서 해당 User ID 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제
        if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
            // Refresh Token 삭제
            redisTemplate.delete("RT:" + authentication.getName());
        }

        // 4. 해당 Access Token 유효시간 가지고 와서 BlackList 로 저장하기
        Long expiration = jwtTokenProvider.getExpiration(logout.getAccessToken());
        redisTemplate.opsForValue()
                .set(logout.getAccessToken(), "logout", expiration, TimeUnit.MILLISECONDS);
        // 5. 이후 JwtAuthenticationFilter 에서 redis 에 있는 logout 정보를 가지고 와서 접근을 거부함

        return ApiResponse.success(USER_LOGOUT_SUCCESS);
    }

    // 회원 정보 조회
    public ApiResponse<UserInfo> getUserInfo(String userId) {
        User user = getUserByUserId(userId);

        return ApiResponse.success(USER_GET_INFO_SUCCESS, UserResponseDto.UserInfo.from(user));
    }

    // 회원 정보 수정
    public ApiResponse<Object> updateUserInfo(String userId, UserRequestDto.UserInfo request) {
        User user = getUserByUserId(userId);

        userRepository.updateUser(
                user.getUserCode(),
                passwordEncoder.encode(request.getUserPwd()),
                request.getUserEmail(),
                request.getUserPhone(),
                request.getUserAddr(),
                request.getUserAddrDetail(),
                request.getUserMainAcc()
        );

        trsfLimitRepository.updateDailyLimit(
                user.getUserCode(),
                request.getUserTrsfLimit()
        );

        return ApiResponse.success(USER_UPDATE_INFO_SUCCESS);
    }

    // 직업 정보 조회
    public ApiResponse<UserResponseDto.UserJobInfo> getUserJobInfo(String userId) {
        User user = getUserByUserId(userId);

        return ApiResponse.success(USER_GET_JOB_INFO_SUCCESS, UserResponseDto.UserJobInfo.from(user.getUserJob()));
    }

    // 직업 정보 수정
    public ApiResponse<Object> updateUserJobInfo(String userId, UserRequestDto.UserJobInfo request) {
        User user = getUserByUserId(userId);

        userJobRepository.updateUserJob(
                user.getUserCode(),
                request.getJobName(),
                request.getCompanyName(),
                request.getCompanyAddr(),
                request.getCompanyPhone()
        );

        return ApiResponse.success(USER_UPDATE_JOB_INFO_SUCCESS);
    }

    public ApiResponse sendSms(String userPhone) {
        try {
            String verificationCode = getVerificationCode();
            smsUtil.sendOne(userPhone, verificationCode);
            smsCertification.createSmsCertification(userPhone, verificationCode);
        } catch (Exception e) {
            throw new CustomException(SMS_SEND_FAIL);
        }
        return ApiResponse.success(SEND_SMS_SUCCESS);
    }

    public ApiResponse verifySms(UserRequestDto.SmsVerify request) {
        if (!isVerify(request)) {
            throw new CustomException(SMS_VERIFY_FAIL);
        }
        smsCertification.deleteSmsCertification(request.getUserPhone());
        return ApiResponse.success(SEND_VERIFY_SUCCESS);
    }

    public ApiResponse<Long> getDailyAccAmount(String userId) {
        User user = getUserByUserId(userId);
        UserTrsfLimit userTrsfLimit = trsfLimitRepository.findById(user.getUserCode())
                .orElseThrow(() -> new CustomException(USER_TRSF_LIMIT_NOT_FOUND));

        return ApiResponse.success(USER_DAILY_AMOUNT_CHECK_SUCCESS, userTrsfLimit.getDailyAccAmount());
    }

    public ApiResponse<Long> getDailyLimit(String userId) {
        User user = getUserByUserId(userId);
        UserTrsfLimit userTrsfLimit = trsfLimitRepository.findById(user.getUserCode())
                .orElseThrow(() -> new CustomException(USER_TRSF_LIMIT_NOT_FOUND));

        return ApiResponse.success(USER_DAILY_AMOUNT_CHECK_SUCCESS, userTrsfLimit.getDailyLimit() - userTrsfLimit.getDailyAccAmount());
    }

    private User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private String getVerificationCode() {
        Random r = new Random();
        String code = "";
        for (int i = 0; i < 6; i++) {
            String random = Integer.toString(r.nextInt(10));
            code += random;
        }

        return code;
    }

    private boolean isVerify(UserRequestDto.SmsVerify request) {
        String storedCode = smsCertification.getSmsCertification(request.getUserPhone());

        if (storedCode == null) {
            log.error("SMS 인증 코드가 Redis에 존재하지 않습니다.");
            return false;
        }

        boolean isMatch = storedCode.equals(request.getVerificationCode());
        log.info("SMS 인증 코드 비교 결과 - 입력 코드: {}, 저장 코드: {}, 일치 여부: {}", request.getVerificationCode(), storedCode, isMatch);

        return isMatch;
    }

    public Optional<User> getUserByUserCode(UUID userCode) {
        return userRepository.findByUserCode(userCode);
    }

    public User findUserByUserId(String userId) {
        return getUserByUserId(userId);
    }

    public ApiResponse<UserResponseDto.UserDetailInfo> getUserDetailInfo(String userId) {
        User user = getUserByUserId(userId);
        return ApiResponse.success(USER_GET_INFO_SUCCESS, UserResponseDto.UserDetailInfo.from(user));
    }
}