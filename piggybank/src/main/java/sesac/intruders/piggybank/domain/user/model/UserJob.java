package sesac.intruders.piggybank.domain.user.model;

import javax.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sesac.intruders.piggybank.global.converter.EncryptConverter;

@Entity(name = "user_job") // entity 이름 정의
@Table(name = "user_job") // Database에 생성될 table의 이름 지정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "user") // user 정보를 출력할 때 무한루프 방지
@Getter
@EntityListeners(AuditingEntityListener.class) // 이벤트가 발생되었을 때 자동 실행
public class UserJob {

    @Id
    @Column(name = "user_code", columnDefinition = "BINARY(16)")
    private UUID userCode;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_code")
    private User user;

    @Column(length = 255)
    @Convert(converter = EncryptConverter.class)
    private String jobName;

    @Column(length = 255)
    @Convert(converter = EncryptConverter.class)
    private String companyName;

    @Column(length = 255)
    @Convert(converter = EncryptConverter.class)
    private String companyAddr;

    @Column(length = 255)
    @Convert(converter = EncryptConverter.class)
    private String companyPhone;

    // Setters for encrypted fields
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyAddr(String companyAddr) {
        this.companyAddr = companyAddr;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }
}
