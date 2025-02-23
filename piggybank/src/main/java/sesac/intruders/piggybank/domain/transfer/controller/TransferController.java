package sesac.intruders.piggybank.domain.transfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesac.intruders.piggybank.domain.transfer.dto.request.AutomaticTransferRequest;
import sesac.intruders.piggybank.domain.transfer.dto.request.ScheduledTransferRequest;
import sesac.intruders.piggybank.domain.transfer.dto.request.TransferRequest;
import sesac.intruders.piggybank.domain.transfer.dto.response.AutomaticTransferResponse;
import sesac.intruders.piggybank.domain.transfer.dto.response.ScheduledTransferResponse;
import sesac.intruders.piggybank.domain.transfer.dto.response.TransferResponse;
import sesac.intruders.piggybank.domain.transfer.repository.AutomaticTransferRepository;
import sesac.intruders.piggybank.domain.transfer.service.AutoTransferListService;
import sesac.intruders.piggybank.domain.transfer.service.AutomaticTransferService;
import sesac.intruders.piggybank.domain.transfer.service.ScheduledTransferService;
import sesac.intruders.piggybank.domain.account.service.AccountService;
import sesac.intruders.piggybank.domain.transfer.dto.response.AutoTransferListResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final AccountService accountService;
    private final ScheduledTransferService scheduledTransferService;
    private final AutomaticTransferService automaticTransferService;
    private final AutomaticTransferRepository automaticTransferRepository;
    private final AutoTransferListService autoTransferListService;
    public TransferController(AccountService accountService,
                              ScheduledTransferService scheduledTransferService,
                              AutomaticTransferService automaticTransferService, AutomaticTransferRepository automaticTransferRepository,
                              AutoTransferListService autoTransferListService) {
        this.accountService = accountService;
        this.scheduledTransferService = scheduledTransferService;
        this.automaticTransferService = automaticTransferService;
        this.automaticTransferRepository = automaticTransferRepository;
        this.autoTransferListService = autoTransferListService;
    }

    // 계좌번호 포맷 처리를 위한 private 메소드 수정
    private String formatAccountNumber(String accountNumber) {
        if (accountNumber == null) return null;
        
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

    // 1. 즉시 계좌 이체
    @PostMapping("/immediately")
    public ResponseEntity<TransferResponse> transferFunds(@RequestBody TransferRequest transferRequest) {
        // 송신자와 수신자의 계좌번호에서 하이픈 제거
        String formattedSenderAccount = formatAccountNumber(transferRequest.getSenderAccountNumber());
        String formattedReceiverAccount = formatAccountNumber(transferRequest.getReceiverAccountNumber());
        
        TransferResponse response = accountService.transferFunds(
                formattedSenderAccount,
                formattedReceiverAccount,
                transferRequest.getAmount(),
                transferRequest.getAccountPassword()
        );
        return ResponseEntity.ok(response);
    }

    // 2. 예약 이체
    @PostMapping("/reservation")
    public ResponseEntity<ScheduledTransferResponse> scheduleTransfer(
            @RequestBody ScheduledTransferRequest scheduledTransferRequest) {
        // 계좌번호 포맷 처리
        scheduledTransferRequest.setSenderAccountNumber(
            formatAccountNumber(scheduledTransferRequest.getSenderAccountNumber()));
        scheduledTransferRequest.setReceiverAccountNumber(
            formatAccountNumber(scheduledTransferRequest.getReceiverAccountNumber()));
        
        ScheduledTransferResponse response = scheduledTransferService.scheduleTransfer(scheduledTransferRequest);
        return ResponseEntity.ok(response);
    }

    // 3. 자동 이체 설정
    @PostMapping("/auto")
    public ResponseEntity<AutomaticTransferResponse> setAutomaticTransfer(
            @RequestBody AutomaticTransferRequest automaticTransferRequest) {
        // 계좌번호 포맷 처리
        String formattedSenderAccount = formatAccountNumber(automaticTransferRequest.getSenderAccountNumber());
        String formattedReceiverAccount = formatAccountNumber(automaticTransferRequest.getReceiverAccountNumber());
            
        AutomaticTransferResponse response = automaticTransferService.setAutomaticTransfer(
                formattedSenderAccount,
                formattedReceiverAccount,
                automaticTransferRequest.getAmount(),
                automaticTransferRequest.getScheduledDate(),
                automaticTransferRequest.getTransferDay()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/autoList/{userCode}")
    public ResponseEntity<List<AutoTransferListResponse>> getAutoTransferList(@PathVariable UUID userCode) {
        List<AutoTransferListResponse> responses = autoTransferListService.getAutoTransferList(userCode);
        return ResponseEntity.ok(responses);
    }
}