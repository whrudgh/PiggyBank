package sesac.intruders.piggybank.domain.transfer.model;
import lombok.Data;
import sesac.intruders.piggybank.domain.account.model.Account;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "transfers")
public class Transfer {

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
    private BigDecimal amount; // 거래 금액

    @Column(name = "transfer_date")
    private LocalDateTime transferDate; // 거래 날짜


}
