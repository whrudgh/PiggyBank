package sesac.intruders.piggybank.domain.user.util;


import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SmsUtil {

    private final Environment env;
    private DefaultMessageService messageService;

    public SmsUtil(Environment env) {
        this.env = env;
    }

    @PostConstruct
    private void init() {
        String apiKey = env.getProperty("coolsms.api.key");
        String apiSecretKey = env.getProperty("coolsms.api.secret");
        String sender = env.getProperty("coolsms.api.sender");

        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.coolsms.co.kr");
    }

    // 메세지 발송
    public SingleMessageSentResponse sendOne(String to, String verificationCode) {
        Message message = new Message();
        message.setFrom(env.getProperty("coolsms.api.sender"));
        message.setTo(to);
        message.setText("[PiggyBank] 본인확인 인증번호는 [" + verificationCode + "]입니다.");

        return this.messageService.sendOne(new SingleMessageSendingRequest(message));
    }
}
