package sesac.intruders.piggybank.domain.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;

import java.time.LocalDateTime;
import java.util.List;

public interface AutomaticTransferRepository extends JpaRepository<Autotransfer, Long> { // 제네릭 타입 추가
    List<Autotransfer> findByIsRecurringFalseAndScheduledDateBeforeAndStatement(
            LocalDateTime scheduledDate, String statement);

    List<Autotransfer> findByIsRecurringTrueAndNextTransferDateBeforeAndStatement(
            LocalDateTime nextTransferDate, String statement);
}