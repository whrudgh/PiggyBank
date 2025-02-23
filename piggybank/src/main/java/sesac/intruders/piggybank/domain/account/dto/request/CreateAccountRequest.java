package sesac.intruders.piggybank.domain.account.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountRequest {
    private String accountNumber;
    private String accountPassword;
    private String pinNumber;
    private String accountType;
    private BigDecimal balance;
}