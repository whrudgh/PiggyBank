package sesac.intruders.piggybank.domain.account.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyAccountPasswordResponse {
    private boolean isValid;
    private String message;

    public static VerifyAccountPasswordResponse success() {
        return VerifyAccountPasswordResponse.builder()
                .isValid(true)
                .message("비밀번호가 일치합니다.")
                .build();
    }

    public static VerifyAccountPasswordResponse fail() {
        return VerifyAccountPasswordResponse.builder()
                .isValid(false)
                .message("비밀번호가 일치하지 않습니다.")
                .build();
    }
}