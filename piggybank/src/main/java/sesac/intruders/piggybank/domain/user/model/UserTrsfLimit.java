package sesac.intruders.piggybank.domain.user.model;

import javax.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "user_trsf_limit") // entity 이름 정의
@Table(name = "user_trsf_limit") // Database에 생성될 table의 이름 지정
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString(exclude = "user") // user 정보를 출력할 때 무한루프 방지
@Getter
@EntityListeners(AuditingEntityListener.class) // 이벤트가 발생되었을 때 자동 실행
public class UserTrsfLimit {

    @Id
    @Column(name = "user_code")
    private UUID userCode;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_code")
    private User user;

    @ColumnDefault("1000000")
    @Builder.Default()
    @Column
    private Long dailyLimit = 1000000L;

    @ColumnDefault("0")
    @Builder.Default()
    @Column
    private Long dailyAccAmount = 0L;

    public void accumulate(Long amount) {
        dailyAccAmount += amount;
    }

    public void resetDailyAccAmount() {
        dailyAccAmount = 0L;
    }
}