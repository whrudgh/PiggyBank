package sesac.intruders.piggybank.domain.account.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyAccountPasswordRequest {
    private String accountNumber;
    private String password;
}