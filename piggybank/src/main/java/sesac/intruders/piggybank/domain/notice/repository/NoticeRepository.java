package sesac.intruders.piggybank.domain.notice.repository;

import sesac.intruders.piggybank.domain.notice.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // SQL Injection에 취약한 네이티브 쿼리
    @Query(value = "SELECT * FROM notices WHERE title LIKE CONCAT('%', ?1, '%')", nativeQuery = true)
    List<Notice> findByTitleContaining(String title);
}
