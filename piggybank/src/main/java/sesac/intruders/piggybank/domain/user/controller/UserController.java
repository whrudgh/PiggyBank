package sesac.intruders.piggybank.domain.user.controller;

import static sesac.intruders.piggybank.global.error.ErrorCode.USER_REGISTER_VALIDATION_FAIL;
import static sesac.intruders.piggybank.global.error.ErrorCode.USER_REISSUE_FAIL;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.Login;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.Register;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.RegisterCheck;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.RegisterCheckEmail;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.RegisterCheckId;
import sesac.intruders.piggybank.domain.user.dto.request.UserRequestDto.Reissue;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.LoginFindId;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.LoginTempPwd;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.RegisterDuplicateCheck;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.TokenInfo;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.UserInfo;
import sesac.intruders.piggybank.domain.user.dto.response.UserResponseDto.UserJobInfo;
import sesac.intruders.piggybank.domain.user.model.Role;
import sesac.intruders.piggybank.domain.user.service.MailService;
import sesac.intruders.piggybank.domain.user.service.UserService;
import sesac.intruders.piggybank.global.common.response.ApiResponse;
import sesac.intruders.piggybank.global.error.exception.CustomException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MailService mailService;

    /* register */

    @Operation(summary = "회원가입 여부 확인")
    @PostMapping("/register/check")
    public ApiResponse<RegisterDuplicateCheck> registerCheck(@RequestBody RegisterCheck request) {
        return userService.registerCheck(request);
    }

    @Operation(summary = "이메일 중복 여부 확인")
    @PostMapping("/register/check-email")
    public ApiResponse<RegisterDuplicateCheck> registerCheckEmail(@RequestBody RegisterCheckEmail request) {
        return userService.registerCheckEmail(request);
    }

    @Operation(summary = "아이디 중복 여부 확인")
    @PostMapping("/register/check-id")
    public ApiResponse<RegisterDuplicateCheck> registerCheckId(@RequestBody RegisterCheckId request) {
        return userService.registerCheckId(request);
    }

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ApiResponse<Object> register(@RequestBody @Validated Register request, Errors errors) {
        // validation check
        if(errors.hasErrors()) {
            throw new CustomException(USER_REGISTER_VALIDATION_FAIL);
        }

        return userService.register(request);
    }

    /* login */
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<TokenInfo> login(@RequestBody Login request) {
        return userService.login(request, Role.ROLE_USER);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/login/reissue")
    public ApiResponse<TokenInfo> reissue(@RequestBody Reissue reissue, Errors errors) {
        // validation check -> ## 어떤 경우에 예외? 강사님께 여쭤보기 ##
        if(errors.hasErrors()) {
            throw new CustomException(USER_REISSUE_FAIL);
        }

        return userService.reissue(reissue);
    }

    @Operation(summary = "아이디 찾기")
    @PostMapping("/login/find-id")
    public ApiResponse<LoginFindId> loginFindId(@RequestBody UserRequestDto.LoginFindId request) {
        return userService.loginFindId(request);
    }

    @Operation(summary = "임시 비밀번호 발급")
    @PostMapping("/login/temp-pwd")
    public ApiResponse<LoginTempPwd> tempPassword(@RequestBody UserRequestDto.LoginTempPwd request) {
        return mailService.tempPasswordEmail(request);
    }

    /* logout */

    @Operation(summary = "로그아웃")
    @PostMapping("/signout")
    public ApiResponse<Object> signout(@RequestBody UserRequestDto.Logout request) {
        return userService.logout(request);
    }

    /* users */

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/users/user-info")
    public ApiResponse<UserInfo> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserInfo(userDetails.getUsername());
    }

    @Operation(summary = "회원 정보 수정")
    @PutMapping("/users/user-info")
    public ApiResponse<Object> updateUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserRequestDto.UserInfo userInfo) {
        return userService.updateUserInfo(userDetails.getUsername(), userInfo);
    }

    @Operation(summary = "직업 정보 조회")
    @GetMapping("/users/job-info")
    public ApiResponse<UserJobInfo> getUserJobInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserJobInfo(userDetails.getUsername());
    }

    @Operation(summary = "직업 정보 수정")
    @PutMapping("/users/job-info")
    public ApiResponse<Object> updateUserJobInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserRequestDto.UserJobInfo userJobInfo) {
        return userService.updateUserJobInfo(userDetails.getUsername(), userJobInfo);
    }

    @Operation(summary = "이메일 인증")
    @PostMapping("/register/authenticate")
    public ApiResponse<String> AuthenticateEmail(@RequestParam("email") String email) {
        return mailService.authenticateEmail(email);
    }

    @Operation(summary = "휴대폰 인증번호 발송")
    @PostMapping("/register/sms-certification/send")
    public ApiResponse sendSms(@RequestBody UserRequestDto.SmsCertificate request) {
        return userService.sendSms(request.getUserPhone());
    }

    @Operation(summary = "휴대폰 인증번호 확인")
    @PostMapping("/register/sms-certification/verify")
    public ApiResponse verifySms(@RequestBody UserRequestDto.SmsVerify request) {
        return userService.verifySms(request);
    }

    @Operation(summary = "오늘 사용한 금액 확인")
    @GetMapping("/users/check-daily-amount")
    public ApiResponse<Long> getDailyAccAmount(@AuthenticationPrincipal UserDetails user) {
        return userService.getDailyAccAmount(user.getUsername());
    }

    @Operation(summary = "오늘 이체 가능한 금액 확인")
    @GetMapping("/users/check-daily-limit")
    public ApiResponse<Long> getDailyLimit(@AuthenticationPrincipal UserDetails user) {
        return userService.getDailyLimit(user.getUsername());
    }

    @Operation(summary = "파일 업로드를 위한 유저 코드 조회")
    @GetMapping("/users/uploader")
    public ApiResponse<UserResponseDto.UserCodeResponse> getUserCode(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserCode(userDetails.getUsername());
    }

    @Operation(summary = "사용자 상세 정보 조회")
    @GetMapping("/user/info")
    public ApiResponse<UserResponseDto.UserDetailInfo> getUserDetailInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return userService.getUserDetailInfo(userDetails.getUsername());
    }

}


