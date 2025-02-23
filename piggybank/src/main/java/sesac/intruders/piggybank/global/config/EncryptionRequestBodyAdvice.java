package sesac.intruders.piggybank.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import sesac.intruders.piggybank.global.dto.EncryptedRequest;
import sesac.intruders.piggybank.global.util.CryptoUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class EncryptionRequestBodyAdvice implements RequestBodyAdvice {

    private final CryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
            Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        // Check if request is from mobile client
        String userAgent = inputMessage.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        log.debug("Received User-Agent: {}", userAgent);
        log.debug("Is Mobile Client: {}", isMobileClient(userAgent));

        if (userAgent == null || !isMobileClient(userAgent)) {
            return inputMessage;
        }

        try {
            String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
            log.debug("=== Request Processing Start ===");
            log.debug("Raw request body: {}", body);
            log.debug("Target type: {}", targetType.getTypeName());
            log.debug("Parameter name: {}", parameter.getParameterName());

            EncryptedRequest encryptedRequest = objectMapper.readValue(body, EncryptedRequest.class);
            log.debug("Encrypted data from request: {}", encryptedRequest.getData());

            String decryptedBody = cryptoUtil.decrypt(encryptedRequest.getData());
            log.debug("Decrypted body: {}", decryptedBody);
            log.debug("=== Request Processing End ===");

            InputStream decryptedStream = new ByteArrayInputStream(decryptedBody.getBytes(StandardCharsets.UTF_8));
            return new HttpInputMessage() {
                @Override
                public InputStream getBody() {
                    return decryptedStream;
                }

                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(inputMessage.getHeaders());
                    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                    return headers;
                }
            };
        } catch (Exception e) {
            log.error("=== Request Processing Error ===");
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new RuntimeException("Failed to decrypt request body", e);
        }
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
            Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
            Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    private boolean isMobileClient(String userAgent) {
        if (userAgent == null)
            return false;
        String lowerAgent = userAgent.toLowerCase();
        log.debug("Checking User-Agent (lowercase): {}", lowerAgent);
        boolean isMobile = lowerAgent.matches(".*(android-piggybank-mobile).*");
        log.debug("Is mobile device: {}", isMobile);
        return isMobile;
    }
}