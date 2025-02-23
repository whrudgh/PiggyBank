package sesac.intruders.piggybank.domain.admin.dto.response;

import sesac.intruders.piggybank.domain.user.model.User;

import java.util.UUID;

/**
 * 관리자가 사용자 정보 리스트를 조회하는 응답 DTO
 */
public record AdminGetUserResponse(
        UUID userCode,
        String userId,
        String userNameKr,
        String userEmail,
        boolean status) {

    public static AdminGetUserResponse of(final User user) {
        return new AdminGetUserResponse(
                user.getUserCode(),
                user.getUserId(),
                user.getUserNameKr(),
                user.getUserEmail(),
                user.isEnabled());
    }
}
