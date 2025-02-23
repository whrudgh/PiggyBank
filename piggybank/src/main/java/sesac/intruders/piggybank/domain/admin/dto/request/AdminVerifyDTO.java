package sesac.intruders.piggybank.domain.admin.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminVerifyDTO {
    private String adminId;
    private String name;
    private String email;
    private String phoneNumber;
}