package sesac.intruders.piggybank.domain.admin.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminChangePasswordDTO {
    private String adminId;
    private String currentPassword;
    private String newPassword;
}