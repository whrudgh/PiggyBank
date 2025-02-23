package sesac.intruders.piggybank.domain.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesac.intruders.piggybank.domain.account.code.AccCodeGenerator;
import sesac.intruders.piggybank.domain.account.dto.request.CreateAccountRequest;
import sesac.intruders.piggybank.domain.account.dto.response.AccountResponse;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.account.service.AccountService;
import sesac.intruders.piggybank.domain.transaction.dto.response.TransactionResponse;
import sesac.intruders.piggybank.domain.transaction.model.Transaction;
import sesac.intruders.piggybank.domain.transaction.service.TransactionService;
import sesac.intruders.piggybank.domain.user.model.User;
import sesac.intruders.piggybank.domain.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import sesac.intruders.piggybank.domain.account.dto.request.VerifyAccountPasswordRequest;
import sesac.intruders.piggybank.domain.account.dto.response.VerifyAccountPasswordResponse;
import sesac.intruders.piggybank.global.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AccCodeGenerator accCodeGenerator;

    // 계좌번호 포맷 처리를 위한 private 메소드
    private String formatAccountNumber(String accountNumber) {
        if (accountNumber == null)
            return null;

        // 모든 하이픈(-) 제거
        String cleanNumber = accountNumber.replaceAll("-", "");

        // XX-XXXXX-XXXXXXX 형식으로 변환
        if (cleanNumber.length() >= 2) {
            StringBuilder formatted = new StringBuilder(cleanNumber);
            formatted.insert(2, "-");
            if (formatted.length() >= 8) {
                formatted.insert(8, "-");
            }
            return formatted.toString();
        }

        return cleanNumber;
    }

    // 계좌 개설
    @PostMapping("/open")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        User user = userService.findUserByUserId(userId);

        String accountNumber;
        do {
            accountNumber = formatAccountNumber(accCodeGenerator.generateAccCode());
        } while (accountService.getAccountByAccountNumber(accountNumber).isPresent());

        // 계좌 생성 시 balance를 0으로 초기화 (null 방지)
        BigDecimal initialBalance = request.getBalance() == null ? BigDecimal.ZERO : request.getBalance();

        // BcryptPasswordEncoder 인스턴스 생성 (필요한 경우 의존성 주입)
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 비밀번호를 Bcrypt로 해싱
        String hashedPassword = passwordEncoder.encode(request.getAccountPassword());

        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountPassword(hashedPassword) // 해싱된 비밀번호 저장
                .pinNumber(request.getPinNumber())
                .accountType(request.getAccountType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .balance(initialBalance)
                .status("ACTIVE")
                .build();

        Account createdAccount = accountService.createAccount(account);

        AccountResponse response = AccountResponse.builder()
                .id(createdAccount.getId())
                .userCode(createdAccount.getUser().getUserCode())
                .accountNumber(createdAccount.getAccountNumber())
                .accountType(createdAccount.getAccountType())
                .balance(createdAccount.getBalance())
                .createdAt(createdAccount.getCreatedAt())
                .updatedAt(createdAccount.getUpdatedAt())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 계좌 목록 조회 (accessToken에서 userCode 추출)
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        User user = userService.findUserByUserId(userId);

        List<AccountResponse> accounts = accountService.getAccountsByUserCode(user.getUserCode()).stream()
                .map(account -> AccountResponse.builder()
                        .id(account.getId())
                        .userCode(account.getUser().getUserCode())
                        .accountNumber(account.getAccountNumber())
                        .accountType(account.getAccountType())
                        .balance(account.getBalance())
                        .createdAt(account.getCreatedAt())
                        .updatedAt(account.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    // 잔액 조회
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestParam("account_number") String accountNumber) {
        String formattedAccountNumber = formatAccountNumber(accountNumber);
        Account account = accountService.getAccountByAccountNumber(formattedAccountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        BigDecimal balance = account.getBalance();

        return new ResponseEntity<>(balance, HttpStatus.OK);
    }

    // 특정 계좌 거래 내역 조회 (계좌 번호 사용)
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccountNumber(
            @RequestParam("account_number") String accountNumber) {
        String formattedAccountNumber = formatAccountNumber(accountNumber);
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(formattedAccountNumber);

        // 거래 내역이 없는 경우, amount와 balanceAfter를 0으로 설정
        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transaction -> TransactionResponse.builder()
                        .id(transaction.getId())
                        .accountNumber(formatAccountNumber(transaction.getAccount().getAccountNumber()))
                        .transactionDate(transaction.getTransactionDate())
                        .transactionType(transaction.getTransactionType())
                        .amount(transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount())
                        .balanceAfter(
                                transaction.getBalanceAfter() == null ? BigDecimal.ZERO : transaction.getBalanceAfter())
                        .build())
                .collect(Collectors.toList());

        return new ResponseEntity<>(transactionResponses, HttpStatus.OK);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<VerifyAccountPasswordResponse> verifyPassword(
            @RequestBody VerifyAccountPasswordRequest request) {
        try {
            boolean isValid = accountService.verifyAccountPassword(
                    request.getAccountNumber(),
                    request.getPassword());

            return ResponseEntity.ok(
                    isValid ? VerifyAccountPasswordResponse.success()
                            : VerifyAccountPasswordResponse.fail());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.ok(VerifyAccountPasswordResponse.fail());
        }
    }
}