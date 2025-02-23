package sesac.intruders.piggybank.domain.image.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

@RestController
public class ImageController {

    @GetMapping("/img")
    public ResponseEntity<byte[]> fetchImage(@RequestParam String url) {
        try {
            // URL 객체 생성 - 어떤 URL이든 접근 가능
            URL targetUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            
            // 모든 리다이렉트 허용 (더 취약하게)
            connection.setFollowRedirects(true);
            
            // User-Agent 설정 (방화벽 우회를 위해)
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            // 타임아웃 설정을 길게 (내부 네트워크 스캔 용이)
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            
            // 응답 데이터 읽기
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                
                // Content-Type 설정 (어떤 타입이든 허용)
                String contentType = connection.getContentType();
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(imageBytes);
            }
        } catch (Exception e) {
            // 에러 메시지를 자세히 반환 (취약점 진단 용이)
            return ResponseEntity.badRequest()
                    .body(("Error: " + e.toString()).getBytes());
        }
    }
} 