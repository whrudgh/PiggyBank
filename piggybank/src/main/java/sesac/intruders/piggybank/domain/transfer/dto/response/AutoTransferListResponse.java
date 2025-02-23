package sesac.intruders.piggybank.domain.transfer.dto.response;

import lombok.Builder;
import lombok.Data;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AutoTransferListResponse {
    private Long id;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private LocalDateTime nextTransferDate;
    private Integer transferDay;
    private Boolean isRecurring;
    private String statement;

    public static AutoTransferListResponse from(Autotransfer autotransfer) {
        Account senderAccount = autotransfer.getSenderAccount();
        Account receiverAccount = autotransfer.getReceiverAccount();

        return AutoTransferListResponse.builder()
                .id(autotransfer.getId())
                .senderAccountNumber(senderAccount.getAccountNumber())
                .receiverAccountNumber(receiverAccount.getAccountNumber())
                .amount(autotransfer.getAmount())
                .nextTransferDate(autotransfer.getNextTransferDate())
                .transferDay(autotransfer.getTransferDay())
                .isRecurring(autotransfer.getIsRecurring())
                .statement(autotransfer.getStatement())
                .build();
    }
}