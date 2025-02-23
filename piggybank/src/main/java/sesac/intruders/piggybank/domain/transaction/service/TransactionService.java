package sesac.intruders.piggybank.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.account.service.AccountService;
import sesac.intruders.piggybank.domain.transaction.model.Transaction;
import sesac.intruders.piggybank.domain.transaction.repository.TransactionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        Account account = accountService.getAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository.findByAccount_Id(account.getId());
    }
}