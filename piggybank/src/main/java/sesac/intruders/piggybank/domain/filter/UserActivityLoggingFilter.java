package sesac.intruders.piggybank.domain.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component // Spring Boot에서 필터 등록
public class UserActivityLoggingFilter implements Filter {
    private static final Logger logger = LogManager.getLogger("UserActionsLogger");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 초기화 필요 시 작성
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String method = httpRequest.getMethod();
            String uri = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String clientIP = httpRequest.getRemoteAddr();
            String userAgent = httpRequest.getHeader("User-Agent");

            // 수정 후 (Log4j Lookup 활성화)
            String logMessage = String.format("Method: %s, URI: %s, Query: %s, IP: %s, User-Agent: %s", 
                method, uri, queryString, clientIP, userAgent);
            logger.info(logMessage);  // 단일 문자열로 로깅
        }
        chain.doFilter(request, response); // 요청을 다음 필터로 전달
    }

    @Override
    public void destroy() {
        // 종료 처리 필요 시 작성
    }
}
