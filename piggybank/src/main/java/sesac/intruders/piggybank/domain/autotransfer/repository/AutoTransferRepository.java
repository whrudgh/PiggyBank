package sesac.intruders.piggybank.domain.autotransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;

import java.time.LocalDateTime;
import java.util.List;


public interface AutoTransferRepository extends JpaRepository<Autotransfer, Long> {
    List<Autotransfer> findByScheduledDateBeforeAndStatement(LocalDateTime scheduledDate, String statement);
}
