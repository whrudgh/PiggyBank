package sesac.intruders.piggybank.domain.admin.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResetPasswordDTO {
    private String adminId;
    private String email;  // 이 이메일로 임시 비밀번호가 전송됨
}