package sesac.intruders.piggybank.domain.admin.dto.response;

import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.user.model.User;

import java.math.BigDecimal;

/**
 * 관리자가 계좌 정보 리스트를 조회하는 응답 DTO
 */
public record AdminGetAccountListResponse(
        String id,
        String userName,
        String accountType,
        BigDecimal balance,
        String status
        ) {

    public static AdminGetAccountListResponse of(final Account account, final User user) {
        return new AdminGetAccountListResponse(
                account.getId().toString(),
                user.getUserNameKr(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus()
        );
    }
}
