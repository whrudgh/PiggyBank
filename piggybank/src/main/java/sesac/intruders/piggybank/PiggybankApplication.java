package sesac.intruders.piggybank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import sesac.intruders.piggybank.global.util.EncryptionUtil;

@SpringBootApplication
@EnableScheduling
public class PiggybankApplication {

public static void main(String[] args) {
    System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "true");
    SpringApplication.run(PiggybankApplication.class, args);
    System.out.println("The encrypted value of 123 is : " + EncryptionUtil.encrypt("홍길동"));
}
}
