package sesac.intruders.piggybank.global.common.response;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // User
    USER_REGISTER_CHECK_SUCCESS(OK, "회원가입 여부 확인 성공"),
    USER_REGISTER_CHECK_EMAIL_SUCCESS(OK, "이메일 중복 확인 성공"),
    USER_REGISTER_CHECK_ID_SUCCESS(OK, "아이디 중복 확인 성공"),
    USER_REGISTER_SUCCESS(CREATED, "회원가입 성공"),
    USER_LOGIN_SUCCESS(OK, "로그인 성공"),
    USER_FIND_ID_SUCCESS(OK, "아이디 찾기 성공"),
    USER_REISSUE_PWD_SUCCESS(OK, "비밀번호 재발급 성공"),
    USER_LOGOUT_SUCCESS(OK, "로그아웃 성공"),
    USER_GET_INFO_SUCCESS(OK, "회원 정보 조회 성공"),
    USER_UPDATE_INFO_SUCCESS(OK, "회원 정보 수정 성공"),
    USER_GET_JOB_INFO_SUCCESS(OK, "직업 정보 조회 성공"),
    USER_UPDATE_JOB_INFO_SUCCESS(OK, "직장 정보 수정 성공"),
    SEND_EMAIL_CODE_SUCCESS(OK, "이메일 인증 코드 전송 성공"),
    USER_REISSUE_SUCCESS(OK, "토큰 재발급 성공"),
    SEND_EMAIL_TEMP_PASSWORD_SUCCESS(OK, "임시 비밀번호 발급 성공"),
    SEND_SMS_SUCCESS(OK, "휴대폰 인증번호 발송 성공"),
    SEND_VERIFY_SUCCESS(OK, "휴대폰 인증번호 검증 성공"),
    USER_DAILY_AMOUNT_CHECK_SUCCESS(OK, "사용자 일일 이체 요금 조회 성공"),

    // Account
    ACCOUNT_CREATE_SUCCESS(CREATED,"계좌 개설 성공"),
    ACCOUNT_DELETE_SUCCESS(OK,"계좌 해지 성공"),
    ACCOUNT_BALANCE_CHECK_SUCCESS(OK, "계좌 잔액 조회 성공"),
    ACCOUNT_SEARCH_SUCCESS(OK, "계좌 조회 성공"),
    ACCOUNT_LIMIT_CHECK_SUCCESS(OK, "이체한도 확인"),
    ACCOUNT_PWD_CHECK_SUCCESS(OK, "계좌 비밀번호 확인"),
    ACCOUNT_TRANSFER_SUCCESS(CREATED, "계좌이체 성공"),
    ACCOUNT_HISTORY_CHECK_SUCCESS(OK, "거래내역 조회 성공"),
    ACCOUNT_LIST_CHECK_SUCCESS(OK, "계좌 잔액 조회 성공"),
    USER_ASSETS_CHECK_SUCCESS(OK, "사용자 자산 조회 성공"),
    ACCOUNT_LIMIT_MODIFY_SUCCESS(OK, "이체 한도 수정 성공"),


    // Product
    PRODUCT_SEARCH_SUCCESS(OK, "상품 조회 성공")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusValue() {
        return httpStatus.value();
    }
}
