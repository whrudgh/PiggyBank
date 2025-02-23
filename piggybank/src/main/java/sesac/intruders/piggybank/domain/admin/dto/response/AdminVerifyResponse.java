package sesac.intruders.piggybank.domain.admin.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminVerifyResponse extends AdminAuthResponseDTO {
    private String verificationToken;
    
    public AdminVerifyResponse(String verificationToken) {
        super("인증이 성공했습니다.", "success", verificationToken);
        this.verificationToken = verificationToken;
    }
    
    public AdminVerifyResponse(String status, String message) {
        super(message, status, null);
        this.verificationToken = null;
    }
}