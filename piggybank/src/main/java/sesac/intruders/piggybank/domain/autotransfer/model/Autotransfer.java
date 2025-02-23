package sesac.intruders.piggybank.domain.autotransfer.model;

import lombok.Data;
import sesac.intruders.piggybank.domain.account.model.Account;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "autotransfer")
public class Autotransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_account_number", referencedColumnName = "account_number")
    private Account senderAccount;

    @ManyToOne
    @JoinColumn(name = "receiver_account_number", referencedColumnName = "account_number")
    private Account receiverAccount;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    @Column(name = "statement")
    private String statement;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledDate;

    @Column(name = "transfer_day")
    private Integer transferDay;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Column(name = "next_transfer_date")
    private LocalDateTime nextTransferDate;

    @Column(name = "account_password")
    private String accountPassword;
}