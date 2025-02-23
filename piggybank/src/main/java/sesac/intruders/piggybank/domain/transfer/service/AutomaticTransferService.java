package sesac.intruders.piggybank.domain.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesac.intruders.piggybank.domain.transfer.dto.request.ScheduledTransferRequest;
import sesac.intruders.piggybank.domain.transfer.dto.response.AutomaticTransferResponse;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.transaction.model.Transaction;
import sesac.intruders.piggybank.domain.transfer.model.Transfer;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;
import sesac.intruders.piggybank.domain.account.repository.AccountRepository;
import sesac.intruders.piggybank.domain.transfer.repository.AutomaticTransferRepository;
import sesac.intruders.piggybank.domain.transfer.repository.TransferRepository;
import sesac.intruders.piggybank.domain.transaction.repository.TransactionRepository;
import sesac.intruders.piggybank.domain.account.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class AutomaticTransferService {
    private static final Logger logger = LoggerFactory.getLogger(AutomaticTransferService.class);

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final AutomaticTransferRepository autoTransferRepository;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public AutomaticTransferService(AccountRepository accountRepository,
                                    TransferRepository transferRepository,
                                    AccountService accountService,
                                    AutomaticTransferRepository autoTransferRepository,
                                    TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.autoTransferRepository = autoTransferRepository;
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    private boolean isAccountActive(Account account) {
        return "ACTIVE".equals(account.getStatus());
    }

    private boolean isTransferCompleted(Autotransfer transfer) {
        return "completed".equals(transfer.getStatement());
    }

    @Transactional
    public synchronized AutomaticTransferResponse scheduleTransfer(ScheduledTransferRequest request) {
        logInfo("Scheduling transfer", "SenderAccount", request.getSenderAccountNumber(), "ReceiverAccount", request.getReceiverAccountNumber());

        Account fromAccount = accountRepository.findByAccountNumber(request.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid sender account number: " + request.getSenderAccountNumber()));
        Account toAccount = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid receiver account number: " + request.getReceiverAccountNumber()));

        if (!isAccountActive(fromAccount) || !isAccountActive(toAccount)) {
            throw new IllegalArgumentException("One or both accounts are not active");
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
        autoTransfer.setAccountPassword(request.getAccountPassword());
        autoTransferRepository.save(autoTransfer);

        logInfo("Scheduled transfer created", "TransferID", autoTransfer.getId());
        return AutomaticTransferResponse.from(autoTransfer);
    }

    @Transactional
    public AutomaticTransferResponse setAutomaticTransfer(String senderAccountNumber, String receiverAccountNumber, BigDecimal amount, LocalDate transferDate, int transferDay) {
        logInfo("Setting automatic transfer", "SenderAccount", senderAccountNumber, "ReceiverAccount", receiverAccountNumber);

        Account fromAccount = accountRepository.findByAccountNumber(senderAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sender account number: " + senderAccountNumber));
        Account toAccount = accountRepository.findByAccountNumber(receiverAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invalid receiver account number: " + receiverAccountNumber));

        if (!isAccountActive(fromAccount) || !isAccountActive(toAccount)) {
            throw new IllegalArgumentException("One or both accounts are not active");
        }

        Autotransfer autoTransfer = new Autotransfer();
        autoTransfer.setSenderAccount(fromAccount);
        autoTransfer.setReceiverAccount(toAccount);
        autoTransfer.setAmount(amount);
        autoTransfer.setScheduledDate(transferDate.atStartOfDay());
        autoTransfer.setStatement("pending");
        autoTransfer.setCreatedAt(LocalDateTime.now());
        autoTransfer.setUpdatedAt(LocalDateTime.now());
        autoTransfer.setIsRecurring(true);
        autoTransfer.setTransferDay(transferDay);

        LocalDateTime nextTransferDate = calculateNextTransferDate(transferDay, transferDate.atStartOfDay());
        autoTransfer.setNextTransferDate(nextTransferDate);

        autoTransferRepository.save(autoTransfer);
        logInfo("Automatic transfer set", "TransferID", autoTransfer.getId(), "NextTransferDate", nextTransferDate);
        return AutomaticTransferResponse.from(autoTransfer);
    }

    @Transactional
    public List<AutomaticTransferResponse> processScheduledTransfers() {
        logInfo("Processing scheduled transfers", "CurrentTime", LocalDateTime.now());

        List<Autotransfer> scheduledTransfers = autoTransferRepository.findByIsRecurringFalseAndScheduledDateBeforeAndStatement(
                LocalDateTime.now(), "pending");

        for (Autotransfer autoTransfer : scheduledTransfers) {
            processTransferWithLogging(autoTransfer);
        }

        logInfo("Scheduled transfers processed", "Count", scheduledTransfers.size());
        return scheduledTransfers.stream()
                .map(AutomaticTransferResponse::from)
                .toList();
    }

    @Transactional
    public List<AutomaticTransferResponse> processRecurringTransfers() {
        logInfo("Processing recurring transfers", "CurrentTime", LocalDateTime.now());

        List<Autotransfer> recurringTransfers = autoTransferRepository.findByIsRecurringTrueAndNextTransferDateBeforeAndStatement(
                LocalDateTime.now(), "pending");

        for (Autotransfer autoTransfer : recurringTransfers) {
            processRecurringTransferWithLogging(autoTransfer);
        }

        logInfo("Recurring transfers processed", "Count", recurringTransfers.size());
        return recurringTransfers.stream()
                .map(AutomaticTransferResponse::from)
                .toList();
    }

    private void processTransferWithLogging(Autotransfer autoTransfer) {
        try {
            processTransfer(autoTransfer);
            autoTransfer.setStatement("completed");
            logInfo("Transfer completed", "TransferID", autoTransfer.getId());
        } catch (Exception e) {
            autoTransfer.setStatement("failed");
            logError("Transfer failed", "TransferID", autoTransfer.getId(), "Error", e.getMessage());
        } finally {
            autoTransfer.setUpdatedAt(LocalDateTime.now());
            autoTransferRepository.save(autoTransfer);
        }
    }

    private void processRecurringTransferWithLogging(Autotransfer autoTransfer) {
        try {
            processTransfer(autoTransfer);

            LocalDateTime nextTransferDate = calculateNextTransferDate(autoTransfer.getTransferDay(), autoTransfer.getNextTransferDate());
            autoTransfer.setNextTransferDate(nextTransferDate);
            autoTransfer.setStatement("pending");

            logInfo("Recurring transfer processed", "TransferID", autoTransfer.getId(), "NextTransferDate", nextTransferDate);
        } catch (Exception e) {
            autoTransfer.setStatement("failed");
            logError("Recurring transfer failed", "TransferID", autoTransfer.getId(), "Error", e.getMessage());
        } finally {
            autoTransfer.setUpdatedAt(LocalDateTime.now());
            autoTransferRepository.save(autoTransfer);
        }
    }

    private LocalDateTime calculateNextTransferDate(int transferDay, LocalDateTime currentDate) {
        if (transferDay >= 28) {
            return currentDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
        return currentDate.plusMonths(1).withDayOfMonth(transferDay);
    }

    private void processTransfer(Autotransfer autoTransfer) {
        Account fromAccount = autoTransfer.getSenderAccount();
        Account toAccount = autoTransfer.getReceiverAccount();

        if (!isAccountActive(fromAccount) || !isAccountActive(toAccount)) {
            throw new IllegalArgumentException("One or both accounts are not active");
        }

        if (!accountService.hasSufficientFunds(fromAccount.getAccountNumber(), autoTransfer.getAmount())) {
            throw new IllegalArgumentException("Insufficient funds in account: " + fromAccount.getAccountNumber());
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(autoTransfer.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(autoTransfer.getAmount()));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transfer transfer = new Transfer();
        transfer.setSenderAccount(fromAccount);
        transfer.setReceiverAccount(toAccount);
        transfer.setAmount(autoTransfer.getAmount());
        transfer.setTransferDate(autoTransfer.getScheduledDate());
        transferRepository.save(transfer);

        saveTransaction(fromAccount, toAccount, autoTransfer.getAmount());
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

        logInfo("Transactions saved", "SenderTransactionID", senderTransaction.getId(), "ReceiverTransactionID", receiverTransaction.getId());
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
