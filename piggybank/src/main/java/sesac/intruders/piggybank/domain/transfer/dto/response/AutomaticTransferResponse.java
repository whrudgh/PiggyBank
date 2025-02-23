package sesac.intruders.piggybank.domain.transfer.dto.response;

import lombok.Builder;
import lombok.Getter;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 자동 이체 응답
@Getter
@Builder
public class AutomaticTransferResponse {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private Integer transferDay; // 이 필드는 Autotransfer에서 가져옵니다.
    private LocalDateTime nextTransferDate;
    private String statement;  // pending, completed, failed
    private Boolean isRecurring;

    public static AutomaticTransferResponse from(Autotransfer autoTransfer) {
        return AutomaticTransferResponse.builder()
                .senderAccountNumber(autoTransfer.getSenderAccount().getAccountNumber())
                .receiverAccountNumber(autoTransfer.getReceiverAccount().getAccountNumber())
                .amount(autoTransfer.getAmount())
                .transferDay(autoTransfer.getTransferDay()) // transferDay는 Autotransfer에서 가져옴
                .nextTransferDate(autoTransfer.getNextTransferDate())
                .statement(autoTransfer.getStatement())
                .isRecurring(autoTransfer.getIsRecurring())
                .build();
    }
}