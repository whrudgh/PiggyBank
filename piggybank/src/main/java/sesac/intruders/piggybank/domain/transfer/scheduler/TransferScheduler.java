package sesac.intruders.piggybank.domain.transfer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sesac.intruders.piggybank.domain.transfer.service.AutomaticTransferService;
import sesac.intruders.piggybank.domain.transfer.service.ScheduledTransferService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferScheduler {

    private final AutomaticTransferService automaticTransferService;
    private final ScheduledTransferService scheduledTransferService;

    // 매 분마다 실행
    @Scheduled(cron = "0 * * * * *")
    public void processAllTransfers() {
        log.info("Starting all transfer processes...");

        // 예약 이체 처리
        try {
            log.info("Processing scheduled transfers...");
            scheduledTransferService.processScheduledTransfers();
            log.info("Completed scheduled transfers.");
        } catch (Exception e) {
            log.error("Error processing scheduled transfers: ", e);
        }

        // 자동 이체 처리
        try {
            log.info("Processing automatic transfers...");
            automaticTransferService.processRecurringTransfers();
            log.info("Completed automatic transfers.");
        } catch (Exception e) {
            log.error("Error processing automatic transfers: ", e);
        }

        log.info("All transfer processes completed.");
    }
}
