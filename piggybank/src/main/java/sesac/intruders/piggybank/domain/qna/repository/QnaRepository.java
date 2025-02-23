package sesac.intruders.piggybank.domain.qna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sesac.intruders.piggybank.domain.qna.model.Qna;

public interface QnaRepository extends JpaRepository<Qna, Long> {}

