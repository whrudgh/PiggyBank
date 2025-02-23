package sesac.intruders.piggybank.domain.transfer.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduledTransferRequest {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private LocalDateTime scheduledDate; // 이 필드가 null이 아닌지 확인
    private String accountPassword;
}