package sesac.intruders.piggybank.domain.transfer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduledTransferResponse {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private LocalDateTime scheduledDate;
    private String statement;  // pending, completed, failed
    private Boolean isRecurring;
    private Integer transferDay; // 추가된 부분, 기본값은 null

    public static ScheduledTransferResponse from(Autotransfer autoTransfer) {
        return ScheduledTransferResponse.builder()
                .senderAccountNumber(autoTransfer.getSenderAccount().getAccountNumber())
                .receiverAccountNumber(autoTransfer.getReceiverAccount().getAccountNumber())
                .amount(autoTransfer.getAmount())
                .scheduledDate(autoTransfer.getScheduledDate())
                .statement(autoTransfer.getStatement())
                .isRecurring(autoTransfer.getIsRecurring())
                .transferDay(null) // transferDay는 null로 설정
                .build();
    }
}
