package sesac.intruders.piggybank.domain.transfer.dto.response;

import lombok.Builder;
import lombok.Getter;
import sesac.intruders.piggybank.domain.transaction.model.Transaction;
import sesac.intruders.piggybank.domain.transfer.model.Transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransferResponse {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private LocalDateTime transferDate;
    private BigDecimal balanceAfter;
    private String statement;  // success, failed 등의 상태

    public static TransferResponse from(Transfer transfer, Transaction transaction) {
        return TransferResponse.builder()
                .senderAccountNumber(transfer.getSenderAccount().getAccountNumber())
                .receiverAccountNumber(transfer.getReceiverAccount().getAccountNumber())
                .amount(transfer.getAmount())
                .transferDate(transfer.getTransferDate())
                .balanceAfter(transaction.getBalanceAfter())
                .statement("success")
                .build();
    }
}