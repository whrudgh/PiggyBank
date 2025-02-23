package sesac.intruders.piggybank.domain.account.code; // 패키지 수정

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component // Lombok @RequiredArgsConstructor 어노테이션 제거
public class AccCodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generateAccCode() {
        StringBuilder accCode = new StringBuilder(16);
        accCode.append("04-");
        for (int i = 0; i < 5; i++) {
            accCode.append(random.nextInt(10));
        }
        accCode.append("-");
        for (int i = 0; i < 7; i++) {
            accCode.append(random.nextInt(10));
        }

        return accCode.toString();
    }
}