package sesac.intruders.piggybank.domain.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sesac.intruders.piggybank.domain.transfer.model.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
