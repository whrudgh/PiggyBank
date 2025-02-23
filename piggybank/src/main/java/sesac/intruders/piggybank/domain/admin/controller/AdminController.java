package sesac.intruders.piggybank.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminUpdateAccountRequest;
import sesac.intruders.piggybank.domain.admin.dto.response.AdminGetAccountListResponse;
import sesac.intruders.piggybank.domain.admin.dto.response.AdminGetUserResponse;
import sesac.intruders.piggybank.domain.admin.service.AdminService;


import java.util.List;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import org.json.JSONObject;
/**
 * 관리자 페이지 컨트롤러
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;

    /**
     * 유저 목록 조회
     * @return 유저 목록
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminGetUserResponse>> getUsers() {
        final List<AdminGetUserResponse> results = adminService.getUsers();
        return ResponseEntity.ok(results);
    }

    /**
     * 유저 정보 업데이트
     */
    @PatchMapping("/users/{userCode}")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable final UUID userCode,
            @RequestBody final AdminUpdateAccountRequest request
    ) {
        adminService.updateUser(userCode, request.status());
        return ResponseEntity.ok().build();
    }

    /**
     * 유저 삭제
     */
    @DeleteMapping("/users/{userCode}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable final UUID userCode
    ) {
        adminService.deleteUser(userCode);
        return ResponseEntity.ok().build();
    }

    /**
     * 계좌 목록 조회
     * @return 계좌 목록
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<AdminGetAccountListResponse>> getAccounts() {
        final List<AdminGetAccountListResponse> results = adminService.getAccounts();
        return ResponseEntity.ok(results);
    }

    /**
     * 계좌 정보 업데이트
     */
    @PatchMapping("/accounts/{id}")
    public ResponseEntity<Void> updateAccount(
            @PathVariable final Integer id,
            @RequestBody final AdminUpdateAccountRequest request
    ) {
        adminService.updateAccount(id, request.status());
        return ResponseEntity.ok().build();
    }

    /**
     * 계좌 삭제
     */
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable final Integer id
    ) {
        adminService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/log-level")
    public ResponseEntity<String> changeLogLevel(@RequestBody String body) {
        try {
            JSONObject jsonObject = new JSONObject(body);
            String level = jsonObject.getString("level").toUpperCase();
            
            // 1. 프로퍼티 설정
            System.setProperty("logLevel", level);
            
            // 2. 특정 로거 강제 갱신
            Configurator.setLevel("UserActionsLogger", Level.valueOf(level));
            Configurator.setRootLevel(Level.valueOf(level));
            
            // 3. 로거 인스턴스 재생성
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            ctx.reconfigure();
            
            logger.info("Changed logLevel: {}", level);
            return ResponseEntity.ok("Success: " + level);
        } catch (Exception e) {
            logger.error("Change failed", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/log-level")
    public ResponseEntity<String> getLogLevel() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        String level = ctx.getConfiguration().getRootLogger().getLevel().toString();

        // 현재 로그 레벨 조회 로깅 (optional)
        logger.info("Current log level: {}", level);

        return new ResponseEntity<>(level, HttpStatus.OK);
    }
}
