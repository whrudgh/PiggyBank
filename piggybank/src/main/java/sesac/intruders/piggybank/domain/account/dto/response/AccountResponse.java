package sesac.intruders.piggybank.domain.account.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sesac.intruders.piggybank.domain.transaction.dto.response.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AccountResponse {
    private int id;
    private UUID userCode; // User 엔티티의 userCode 참조
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Setter
    private List<TransactionResponse> transactions; // 거래 내역 추가
}