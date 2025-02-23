package sesac.intruders.piggybank.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.admin.dto.response.AdminGetAccountListResponse;
import sesac.intruders.piggybank.domain.admin.dto.response.AdminGetUserResponse;
import sesac.intruders.piggybank.domain.account.repository.AccountRepository;
import sesac.intruders.piggybank.domain.user.model.User;
import sesac.intruders.piggybank.domain.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.LogManager;

import lombok.extern.slf4j.Slf4j;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminLoginDTO;
import sesac.intruders.piggybank.domain.admin.dto.response.AdminLoginResponse;
import sesac.intruders.piggybank.domain.admin.model.Admin;
import sesac.intruders.piggybank.domain.admin.repository.AdminRepository;
import sesac.intruders.piggybank.global.security.jwt.JwtTokenProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class AdminService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 유저 정보 업데이트
     * 
     * @param userCode 유저 코드
     * @param status   유저 상태
     */
    public void updateUser(final UUID userCode, final String status) {
        final User user = userRepository.findById(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 현재 유저 상태에 따른 정보 복사
        if ("true".equalsIgnoreCase(status)) {
            user.update("ACTIVE"); // update 메서드 호출
        } else if ("false".equalsIgnoreCase(status)) {
            user.update("INACTIVE"); // update 메서드 호출
        } else {
            throw new IllegalArgumentException("Invalid status provided");
        }

        // 현재 유저 상태 저장
        userRepository.save(user);
    }

    /**
     * 유저 삭제
     * 유저의 계좌도 합께 삭제한다.
     * 
     * @param userCode 유저 코드
     */
    public void deleteUser(final UUID userCode) {
        final User user = userRepository.findById(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        accountRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }

    /**
     * 유저 목록 조회
     * 
     * @return 유저 목록
     */
    public List<AdminGetUserResponse> getUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() != null) // null status 필터링
                .map(AdminGetUserResponse::of)
                .collect(Collectors.toList());
    }

    /**
     * 계좌 정보 업데이트
     * 
     * @param accountId 계좌 ID
     * @param status    계좌 상태
     */
    public void updateAccount(final Integer accountId, final String status) {
        final Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.update(status);
        accountRepository.save(account);
    }

    /**
     * 계좌 삭제
     * 
     * @param id 계좌 ID
     */
    public void deleteAccount(final Integer id) {
        final Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        accountRepository.delete(account);
    }

    /**
     * 계좌 목록 조회
     * 
     * @return 계좌 목록
     */
    public List<AdminGetAccountListResponse> getAccounts() {
        return accountRepository.findAll().stream()
                .map(account -> AdminGetAccountListResponse.of(account, account.getUser()))
                .toList();
    }

    // log
    public void changeLogLevel(String level) {
        Level newLevel = Level.valueOf(level.toUpperCase());
        Configurator.setRootLevel(newLevel);
    }

    public AdminLoginResponse login(AdminLoginDTO loginDTO) {
        try {
            Admin admin = adminRepository.findByAdminId(loginDTO.getAdminId())
                    .orElseThrow(() -> new RuntimeException("Invalid admin credentials"));

            if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
                return AdminLoginResponse.error();
            }

            String token = jwtTokenProvider.generateAdminToken(admin.getAdminId(), "ROLE_ADMIN");
            return AdminLoginResponse.success(token);
        } catch (Exception e) {
            return AdminLoginResponse.error();
        }
    }
}
