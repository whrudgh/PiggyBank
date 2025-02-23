package sesac.intruders.piggybank.domain.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUser_UserCode(UUID userCode);
    void deleteAllByUser(User user);

    Account findByAccountPassword(String accountPassword);
}
