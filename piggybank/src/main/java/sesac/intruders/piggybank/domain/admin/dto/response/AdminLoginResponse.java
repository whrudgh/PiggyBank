package sesac.intruders.piggybank.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminLoginResponse {
    private String message;
    private String status;
    private String token;

    public static AdminLoginResponse success(String token) {
        return AdminLoginResponse.builder()
                .message("관리자 로그인 성공")
                .status("success")
                .token(token)
                .build();
    }

    public static AdminLoginResponse error() {
        return AdminLoginResponse.builder()
                .message("로그인 정보가 올바르지 않습니다")
                .status("error")
                .build();
    }
}