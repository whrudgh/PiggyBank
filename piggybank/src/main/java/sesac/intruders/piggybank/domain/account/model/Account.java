package sesac.intruders.piggybank.domain.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Convert;

import lombok.*;
import sesac.intruders.piggybank.domain.user.model.User;
import sesac.intruders.piggybank.global.converter.EncryptConverter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Accounts")
public class Account implements Serializable {

    private static final long serialVersionUID = 1L; // 직렬화 버전 관리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", insertable = true, updatable = false, columnDefinition = "BINARY(16)")
    private User user;

    @Column(name = "account_number", nullable = false, length = 255)
    @Convert(converter = EncryptConverter.class)
    private String accountNumber;

    @Column(name = "account_password")
    private String accountPassword;

    @Column(name = "pin_number", length = 255)
    @Convert(converter = EncryptConverter.class)
    private String pinNumber;

    @Column(length = 10)
    private String status;

    @Column(name = "account_type", length = 50)
    private String accountType;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(final String status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
