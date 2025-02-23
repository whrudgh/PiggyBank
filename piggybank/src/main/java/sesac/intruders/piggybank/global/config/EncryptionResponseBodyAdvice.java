package sesac.intruders.piggybank.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import sesac.intruders.piggybank.global.exception.EncryptionException;
import sesac.intruders.piggybank.global.util.CryptoUtil;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class EncryptionResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final CryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Skip encryption for Swagger UI responses
        String path = returnType.getContainingClass().getName();
        return !path.contains("springfox") && !path.contains("swagger");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        // Check if request is from mobile client
        String userAgent = request.getHeaders().getFirst("User-Agent");
        log.debug("Response - Received User-Agent: {}", userAgent);
        log.debug("Response - Is Mobile Client: {}", isMobileClient(userAgent));

        if (userAgent == null || !isMobileClient(userAgent)) {
            log.debug("Response - Not a mobile client, returning original body");
            return body;
        }

        try {
            if (body == null) {
                log.debug("Response - Body is null, returning null");
                return null;
            }

            String json = objectMapper.writeValueAsString(body);
            log.debug("Response - Original body: {}", json);

            String encryptedData = cryptoUtil.encrypt(json);
            log.debug("Response - Encrypted data: {}", encryptedData);

            Map<String, String> encryptedResponse = new HashMap<>();
            encryptedResponse.put("data", encryptedData);
            log.debug("Response - Final encrypted response: {}", objectMapper.writeValueAsString(encryptedResponse));

            return encryptedResponse;
        } catch (Exception e) {
            log.error("Response - Failed to encrypt response body", e);
            throw new EncryptionException("응답 데이터 암호화에 실패했습니다.", e);
        }
    }

    private boolean isMobileClient(String userAgent) {
        if (userAgent == null)
            return false;
        String lowerAgent = userAgent.toLowerCase();
        log.debug("Response - Checking User-Agent (lowercase): {}", lowerAgent);
        boolean isMobile = lowerAgent.matches(".*(android-piggybank-mobile).*");
        log.debug("Response - Is mobile device: {}", isMobile);
        return isMobile;
    }
}