package sesac.intruders.piggybank.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {
    private int id;
//    private int accountId; // Account ID
    private String accountNumber;
    private LocalDateTime transactionDate;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
}