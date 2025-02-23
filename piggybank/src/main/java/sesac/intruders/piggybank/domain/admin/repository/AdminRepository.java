package sesac.intruders.piggybank.domain.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sesac.intruders.piggybank.domain.admin.model.Admin;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminId(String adminId);
}


