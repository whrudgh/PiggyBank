package sesac.intruders.piggybank.domain.admin.dto.request;

/**
 * 관리자가 계좌 정보를 업데이트하는 요청 DTO
 * @param status
 */
public record AdminUpdateAccountRequest(
        String status
        ) {
}