package sesac.intruders.piggybank.domain.transfer.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AutomaticTransferRequest {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private LocalDate scheduledDate;
    private String accountPassword;
    private Integer transferDay;
}