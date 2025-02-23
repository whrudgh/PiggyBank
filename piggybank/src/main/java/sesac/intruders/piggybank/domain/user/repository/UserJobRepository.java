package sesac.intruders.piggybank.domain.user.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sesac.intruders.piggybank.domain.user.model.UserJob;

@Repository
public interface UserJobRepository extends JpaRepository<UserJob, UUID> {
    // 직업 정보 수정
    @Modifying(clearAutomatically = true) // bulk 연산 실행 후 1차 cache를 비워준다
                                          // 쿼리 수행 후 1차 cache와 DB의 동기화를 위해 추가
    @Query("update user_job uj set uj.jobName = :jobName, uj.companyName = :companyName, uj.companyAddr = :companyAddr, uj.companyPhone = :companyPhone where uj.userCode = :userCode")
    void updateUserJob(UUID userCode, String jobName, String companyName, String companyAddr, String companyPhone);
}
