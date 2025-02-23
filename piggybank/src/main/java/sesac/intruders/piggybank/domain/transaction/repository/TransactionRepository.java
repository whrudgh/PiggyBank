package sesac.intruders.piggybank.domain.transaction.repository;
import sesac.intruders.piggybank.domain.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository <Transaction, Long> {
    List<Transaction> findByAccount_Id(int accountId);
}
