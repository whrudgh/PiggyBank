package sesac.intruders.piggybank.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String[] key(); // 락 이름 (고유값)
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    long waitTime() default 5L; // 락 획득을 시도하는 최대 시간
    long leaseTime() default 3L; // 락을 획득한 후 점유하는 최대 시간
}
