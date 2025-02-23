package sesac.intruders.piggybank.domain.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesac.intruders.piggybank.domain.transfer.dto.request.ScheduledTransferRequest;
import sesac.intruders.piggybank.domain.transfer.dto.response.ScheduledTransferResponse;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.account.repository.AccountRepository;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;
import sesac.intruders.piggybank.domain.autotransfer.repository.AutoTransferRepository;
import sesac.intruders.piggybank.domain.transaction.model.Transaction;
import sesac.intruders.piggybank.domain.transaction.repository.TransactionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduledTransferService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTransferService.class);

    private final AccountRepository accountRepository;
    private final AutoTransferRepository autoTransferRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public ScheduledTransferService(AccountRepository accountRepository,
                                    AutoTransferRepository autoTransferRepository,
                                    TransactionRepository transactionRepository,
                                    PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.autoTransferRepository = autoTransferRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ScheduledTransferResponse scheduleTransfer(ScheduledTransferRequest request) {
        logInfo("Starting to schedule transfer", "SenderAccount", request.getSenderAccountNumber(), "ReceiverAccount", request.getReceiverAccountNumber());

        Account fromAccount = accountRepository.findByAccountNumber(request.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid sender account number"));
        Account toAccount = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid receiver account number"));

        if (!passwordEncoder.matches(request.getAccountPassword(), fromAccount.getAccountPassword())) {
            logError("Invalid password for account", "Account", fromAccount.getAccountNumber());
            throw new IllegalArgumentException("Invalid password for account: " + fromAccount.getAccountNumber());
        }

        Autotransfer autoTransfer = new Autotransfer();
        autoTransfer.setSenderAccount(fromAccount);
        autoTransfer.setReceiverAccount(toAccount);
        autoTransfer.setAmount(request.getAmount());
        autoTransfer.setScheduledDate(request.getScheduledDate());
        autoTransfer.setStatement("pending");
        autoTransfer.setCreatedAt(LocalDateTime.now());
        autoTransfer.setUpdatedAt(LocalDateTime.now());
        autoTransfer.setIsRecurring(false);

        autoTransferRepository.save(autoTransfer);

        logInfo("Scheduled transfer created successfully", "TransferID", autoTransfer.getId());
        return ScheduledTransferResponse.from(autoTransfer);
    }

    @Transactional
    public List<ScheduledTransferResponse> processScheduledTransfers() {
        logInfo("Processing scheduled transfers", "CurrentTime", LocalDateTime.now().toString());

        List<Autotransfer> scheduledTransfers = autoTransferRepository.findByScheduledDateBeforeAndStatement(
                LocalDateTime.now(), "pending"
        );

        scheduledTransfers.forEach(this::processSingleTransfer);

        logInfo("Scheduled transfers processed", "Count", scheduledTransfers.size());
        return scheduledTransfers.stream()
                .map(ScheduledTransferResponse::from)
                .collect(Collectors.toList());
    }

    private void processSingleTransfer(Autotransfer autoTransfer) {
        try {
            logInfo("Processing transfer", "TransferID", autoTransfer.getId());

            Account sender = autoTransfer.getSenderAccount();
            Account receiver = autoTransfer.getReceiverAccount();
            BigDecimal amount = autoTransfer.getAmount();

            if (sender.getBalance().compareTo(amount) < 0) {
                autoTransfer.setStatement("failed");
                logError("Insufficient funds", "TransferID", autoTransfer.getId(), "SenderAccount", sender.getAccountNumber());
                return;
            }

            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));

            autoTransfer.setTransferDate(LocalDateTime.now());
            autoTransfer.setStatement("completed");
            logInfo("Transfer completed successfully", "TransferID", autoTransfer.getId());

            saveTransaction(sender, receiver, amount);

        } catch (Exception e) {
            autoTransfer.setStatement("failed");
            logError("Failed to process transfer", "TransferID", autoTransfer.getId(), "Error", e.getMessage());
        } finally {
            autoTransfer.setUpdatedAt(LocalDateTime.now());
            autoTransferRepository.save(autoTransfer);
        }
    }

    private void saveTransaction(Account sender, Account receiver, BigDecimal amount) {
        logInfo("Saving transactions", "SenderAccount", sender.getAccountNumber(), "ReceiverAccount", receiver.getAccountNumber());

        Transaction senderTransaction = new Transaction();
        senderTransaction.setAccount(sender);
        senderTransaction.setTransactionDate(LocalDateTime.now());
        senderTransaction.setTransactionType("debit");
        senderTransaction.setAmount(amount.negate());
        senderTransaction.setBalanceAfter(sender.getBalance());
        transactionRepository.save(senderTransaction);

        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setAccount(receiver);
        receiverTransaction.setTransactionDate(LocalDateTime.now());
        receiverTransaction.setTransactionType("credit");
        receiverTransaction.setAmount(amount);
        receiverTransaction.setBalanceAfter(receiver.getBalance());
        transactionRepository.save(receiverTransaction);

        logInfo("Transactions saved successfully", "SenderTransactionID", senderTransaction.getId(), "ReceiverTransactionID", receiverTransaction.getId());
    }

    private void logInfo(String message, Object... args) {
        logger.info(formatLogMessage(message, args));
    }

    private void logError(String message, Object... args) {
        logger.error(formatLogMessage(message, args));
    }

    private String formatLogMessage(String message, Object... args) {
        StringBuilder sb = new StringBuilder(message);
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                sb.append(" | ").append(args[i]).append(": ").append(args[i + 1]);
            }
        }
        return sb.toString();
    }
}
