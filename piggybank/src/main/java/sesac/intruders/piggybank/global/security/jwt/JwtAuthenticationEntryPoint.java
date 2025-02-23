package sesac.intruders.piggybank.global.security.jwt;

import static sesac.intruders.piggybank.global.error.ErrorCode.USER_AUTHENTICATION_FAIL;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sesac.intruders.piggybank.global.error.ErrorResponse;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.error("토큰 유휴성 검사에 실패했습니다.");
        log.error("Responding with unauthorized error. Message - {}", authException.getMessage());

        // ErrorResponse 객체 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(202)
                .code(USER_AUTHENTICATION_FAIL.getCode())
                .message(USER_AUTHENTICATION_FAIL.getMessage())
                .build();

        // JSON 응답을 작성하기 위해 content type을 설정합니다.
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_ACCEPTED); // 202 Accepted 상태 코드 설정

        // ErrorResponse 객체를 JSON 문자열로 변환
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        // JSON 응답을 클라이언트에 전송
        response.getWriter().write(jsonResponse);
    }

}