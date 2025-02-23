package sesac.intruders.piggybank.domain.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import sesac.intruders.piggybank.domain.admin.dto.request.AdminChangePasswordDTO;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminLoginDTO;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminResetPasswordDTO;
import sesac.intruders.piggybank.domain.admin.dto.request.AdminVerifyDTO;
import sesac.intruders.piggybank.domain.admin.dto.response.AdminAuthResponseDTO;
import sesac.intruders.piggybank.domain.admin.dto.response.AdminVerifyResponse;
import sesac.intruders.piggybank.domain.admin.service.AdminAuthService;
import sesac.intruders.piggybank.domain.notice.dto.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {
    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponseDTO> login(@RequestBody AdminLoginDTO loginDTO) {
        try {
            String token = adminAuthService.login(loginDTO);
            return ResponseEntity.ok(AdminAuthResponseDTO.success(token));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(AdminAuthResponseDTO.error("로그인 정보가 올바르지 않습니다"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AdminAuthResponseDTO> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7); // "Bearer " 제거
        adminAuthService.logout(token);
        return ResponseEntity.ok(AdminAuthResponseDTO.success("로그아웃 되었습니다."));
    }

    @PostMapping("/verify")
    public ResponseEntity<AdminVerifyResponse> verify(@RequestBody AdminVerifyDTO request) {
        try {
            String verificationToken = adminAuthService.verifyAdminInfo(request);
            return ResponseEntity.ok(new AdminVerifyResponse(verificationToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(202).body(new AdminVerifyResponse("E105", "사용자 인증 실패"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AdminAuthResponseDTO> resetPassword(
            @RequestHeader("X-Verification-Token") String verificationToken,
            @RequestBody AdminResetPasswordDTO request) {
        try {
            adminAuthService.resetPassword(verificationToken, request);
            return ResponseEntity.ok(AdminAuthResponseDTO.success("임시 비밀번호가 이메일로 전송되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(AdminAuthResponseDTO.error(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/change-password")
    public ResponseEntity<AdminAuthResponseDTO> changePassword(@RequestBody AdminChangePasswordDTO request) {
        try {
            // 현재 로그인한 관리자 검증
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentAdminId = authentication.getName();
            
            if (!currentAdminId.equals(request.getAdminId())) {
                return ResponseEntity.badRequest().body(AdminAuthResponseDTO.error("본인의 비밀번호만 변경할 수 있습니다."));
            }
            
            adminAuthService.changePassword(request);
            return ResponseEntity.ok(AdminAuthResponseDTO.success("비밀번호가 성공적으로 변경되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(AdminAuthResponseDTO.error(e.getMessage()));
        }
    }
}
