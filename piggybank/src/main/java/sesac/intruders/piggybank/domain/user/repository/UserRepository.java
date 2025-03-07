package sesac.intruders.piggybank.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sesac.intruders.piggybank.domain.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // 회원가입 여부 확인
    Boolean existsByUserNameKrAndUserPhone(String userNameKr, String userPhone);

    // 중복 이메일 확인
    Boolean existsByUserEmail(String userEmail);

    // 중복 ID 확인
    Boolean existsByUserId(String userId);

    Optional<User> findByUserId(String userId);
    Optional<User> findByUserCode(UUID userCode);

    // 아이디 조회
    @Query("select u.userId from user u where u.userNameKr = :userNameKr and u.userEmail = :userEmail")
    Optional<String> findUserIdByUserNameKrAndUserEmail(String userNameKr, String userEmail);

    // 임시 비밀번호 발급 - 계정 조회
    Optional<User> findByUserNameKrAndUserIdAndUserEmail(String userNameKr, String userId, String userEmail);

    // 비밀번호 변경
    @Modifying(clearAutomatically = true) // bulk 연산 실행 후 1차 cache를 비워준다
    // 쿼리 수행 후 1차 cache와 DB의 동기화를 위해 추가
    @Transactional
    @Query("update user u set u.userPwd = :userPwd where u.userCode = :userCode")
    void updateUserPwd(UUID userCode, String userPwd);

    // 마이페이지 - 회원 정보 수정
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update user u set u.userPwd = :userPwd, u.userEmail = :userEmail, u.userPhone = :userPhone, u.userAddr = :userAddr, u.userAddrDetail = :userAddrDetail, u.userMainAcc = :userMainAcc where u.userCode = :userCode")
    void updateUser(UUID userCode, String userPwd, String userEmail, String userPhone, String userAddr, String userAddrDetail, String userMainAcc);

    // 유저 일일 계좌 이체 한도 찾기
    @Query("select utl.dailyLimit from user_trsf_limit utl where utl.user.userId =:userId")
    Optional<Long> getUserLimit(String userId);

    // userId로 userCode 조회
    @Query("SELECT u.userCode FROM user u WHERE u.userId = :userId")
    Optional<UUID> findUserCodeByUserId(@Param("userId") String userId);

}
