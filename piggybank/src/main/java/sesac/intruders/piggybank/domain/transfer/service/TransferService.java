package sesac.intruders.piggybank.domain.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sesac.intruders.piggybank.domain.transfer.dto.response.TransferResponse;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.account.repository.AccountRepository;
import sesac.intruders.piggybank.domain.transfer.model.Transfer;
import sesac.intruders.piggybank.domain.transfer.repository.TransferRepository;
import sesac.intruders.piggybank.domain.transaction.repository.TransactionRepository;
import sesac.intruders.piggybank.domain.transaction.model.Transaction;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransferService {
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final PasswordEncoder passwordEncoder;

    public TransferService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           TransferRepository transferRepository,
                           PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isAccountActive(Account account) {
        return "ACTIVE".equals(account.getStatus());
    }

    private boolean isAccountOwner(Account account) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return account.getUser().getUsername().equals(username);
        } else {
            throw new IllegalStateException("Authentication principal is not of type UserDetails");
        }
    }

    public boolean hasSufficientFunds(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            if (!isAccountActive(account)) {
                throw new IllegalArgumentException("This account is not active");
            }
            if (!isAccountOwner(account)) {
                throw new IllegalArgumentException("This account does not belong to the current user");
            }
            return account.getBalance().compareTo(amount) >= 0;
        }
        return false;
    }

    public boolean verifyPassword(String accountNumber, String password) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account number: " + accountNumber));
        return passwordEncoder.matches(password, account.getAccountPassword());
    }

    @Transactional
    public TransferResponse transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String password) {
        if (!verifyPassword(fromAccountNumber, password)) {
            throw new IllegalArgumentException("Invalid password for account: " + fromAccountNumber);
        }

        Optional<Account> fromAccountOptional = accountRepository.findByAccountNumber(fromAccountNumber);
        Optional<Account> toAccountOptional = accountRepository.findByAccountNumber(toAccountNumber);

        if (fromAccountOptional.isPresent() && toAccountOptional.isPresent()) {
            Account fromAccount = fromAccountOptional.get();
            Account toAccount = toAccountOptional.get();

            if (!isAccountActive(fromAccount)) {
                throw new IllegalArgumentException("Sender account is not active");
            }
            if (!isAccountActive(toAccount)) {
                throw new IllegalArgumentException("Receiver account is not active");
            }
            if (!isAccountOwner(fromAccount)) {
                throw new IllegalArgumentException("Sender account does not belong to the current user");
            }

            if (!hasSufficientFunds(fromAccountNumber, amount)) {
                throw new IllegalArgumentException("Insufficient funds");
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            Transfer transfer = new Transfer();
            transfer.setSenderAccount(fromAccount);
            transfer.setReceiverAccount(toAccount);
            transfer.setAmount(amount);
            transfer.setTransferDate(LocalDateTime.now());
            Transfer savedTransfer = transferRepository.save(transfer);

            Transaction senderTransaction = new Transaction();
            senderTransaction.setAccount(fromAccount);
            senderTransaction.setTransactionDate(LocalDateTime.now());
            senderTransaction.setTransactionType("debit");
            senderTransaction.setAmount(amount.negate());
            senderTransaction.setBalanceAfter(fromAccount.getBalance());
            transactionRepository.save(senderTransaction);

            Transaction receiverTransaction = new Transaction();
            receiverTransaction.setAccount(toAccount);
            receiverTransaction.setTransactionDate(LocalDateTime.now());
            receiverTransaction.setTransactionType("credit");
            receiverTransaction.setAmount(amount);
            receiverTransaction.setBalanceAfter(toAccount.getBalance());
            transactionRepository.save(receiverTransaction);

            return TransferResponse.from(savedTransfer, senderTransaction);
        } else {
            throw new IllegalArgumentException("Invalid account numbers provided for the transfer.");
        }
    }
}
