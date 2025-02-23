package sesac.intruders.piggybank.domain.user.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SmsCertification {

    private final String PREFIX = "sms:";
    private final int LIMIT_TIME = 3 * 60;

    private final RedisTemplate redisTemplate;

    public void createSmsCertification(String phone, String verificationCode) {
        redisTemplate.opsForValue()
                .set(PREFIX + phone, verificationCode, Duration.ofSeconds(LIMIT_TIME));
    }

    public String getSmsCertification(String phone) {
        return (String) redisTemplate.opsForValue().get(PREFIX + phone);
    }

    public void deleteSmsCertification(String phone) {
        redisTemplate.delete(PREFIX + phone);
    }

    public boolean hasKey(String phone) {
        return redisTemplate.hasKey(PREFIX + phone);
    }
}