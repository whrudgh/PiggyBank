// 1. Repository 수정
package sesac.intruders.piggybank.domain.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sesac.intruders.piggybank.domain.autotransfer.model.Autotransfer;

import java.util.List;
import java.util.UUID;

public interface AutotransferListRepository extends JpaRepository<Autotransfer, Long> {
    @Query("SELECT at FROM Autotransfer at " +
            "WHERE at.senderAccount.user.userCode = :userCode " +
            "AND at.isRecurring = true")
    List<Autotransfer> findByUserCodeAndIsRecurringTrue(@Param("userCode") UUID userCode);
}