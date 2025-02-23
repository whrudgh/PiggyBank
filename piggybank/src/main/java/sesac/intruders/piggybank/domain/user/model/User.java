package sesac.intruders.piggybank.domain.user.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sesac.intruders.piggybank.global.converter.EncryptConverter;
// import sesac.intruders.piggybank.domain.account.entity.Account;

@Entity(name = "user") // entity 이름 정의
@Table(name = "user") // Database에 생성될 table의 이름 지정
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
@Getter
@EntityListeners(AuditingEntityListener.class) // 이벤트가 발생되었을 때 자동 실행
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_code", columnDefinition = "BINARY(16)")
    private UUID userCode;

    @Column(unique = true)
    @NotNull
    private String userId;

    @Column
    @NotNull
    private String userPwd;

    @Column(length = 255)
    @NotNull
    @Convert(converter = EncryptConverter.class)
    private String userNameKr;

    @Column(length = 255)
    @NotNull
    @Convert(converter = EncryptConverter.class)
    private String userNameEn;

    @Column(updatable = false, length = 255)
    @NotNull
    @Convert(converter = EncryptConverter.class)
    private String userInherentNumber;

    @Column(unique = true, length = 255)
    @NotNull
    @Convert(converter = EncryptConverter.class)
    private String userEmail;

    @Column(length = 255)
    @NotNull
    @Convert(converter = EncryptConverter.class)
    private String userPhone;

    @Column(length = 255)
    @NotNull
    @Convert(converter = EncryptConverter.class)
    private String userAddr;

    // User 상태 관리를 위한 컬럼 추가
    @Column
    @Builder.Default
    private String status = "ACTIVE"; // 기본값 설정

    @Column(length = 255)
    @NotNull
    @Convert(converter = EncryptConverter.class)
    private String userAddrDetail;

    @CreatedDate
    @Column(updatable = false) // 컬럼 수정 불가
    private LocalDate userCreatedAt;

    @Column
    private String userMainAcc;

    @OneToOne(mappedBy = "user") // cf."user_job"이 아니라 "user"
    private UserJob userJob;

    @OneToOne(mappedBy = "user")
    private UserTrsfLimit userTrsfLimit;

    // 권한 목록
    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    // User 상태 관리를 위한 세터 메서드 추가
    public void update(final String status) {
        this.status = status;
    }

    /* Override */

    // 해당 유저의 권한 목록
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public String getPassword() {
        return userPwd;
    }

    // 계정 만료 여부 (true: 만료 안 됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 (true: 만료 안 됨)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 사용자 활성화 여부 (true: 활성화)
    @Override
    public boolean isEnabled() {
        if (status.equals("ACTIVE")) {
            return true;
        }
        return false;
    }

    // Setters for encrypted fields
    public void setUserNameKr(String userNameKr) {
        this.userNameKr = userNameKr;
    }

    public void setUserNameEn(String userNameEn) {
        this.userNameEn = userNameEn;
    }

    public void setUserInherentNumber(String userInherentNumber) {
        this.userInherentNumber = userInherentNumber;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setUserAddr(String userAddr) {
        this.userAddr = userAddr;
    }

    public void setUserAddrDetail(String userAddrDetail) {
        this.userAddrDetail = userAddrDetail;
    }
}
