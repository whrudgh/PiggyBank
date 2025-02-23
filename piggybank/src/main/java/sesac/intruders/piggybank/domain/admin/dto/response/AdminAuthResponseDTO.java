package sesac.intruders.piggybank.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthResponseDTO {
    private String message;
    private String status;
    private String token;

    public static AdminAuthResponseDTO success(String token) {
        return AdminAuthResponseDTO.builder()
                .message("관리자 로그인 성공")
                .status("success")
                .token(token)
                .build();
    }

    public static AdminAuthResponseDTO error(String message) {
        return AdminAuthResponseDTO.builder()
                .message(message)
                .status("error")
                .build();
    }
}