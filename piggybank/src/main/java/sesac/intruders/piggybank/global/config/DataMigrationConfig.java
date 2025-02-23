package sesac.intruders.piggybank.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import sesac.intruders.piggybank.domain.account.model.Account;
import sesac.intruders.piggybank.domain.account.repository.AccountRepository;
import sesac.intruders.piggybank.domain.admin.model.Admin;
import sesac.intruders.piggybank.domain.admin.repository.AdminRepository;
import sesac.intruders.piggybank.domain.user.model.User;
import sesac.intruders.piggybank.domain.user.model.UserJob;
import sesac.intruders.piggybank.domain.user.repository.UserJobRepository;
import sesac.intruders.piggybank.domain.user.repository.UserRepository;
import sesac.intruders.piggybank.global.util.EncryptionUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataMigrationConfig {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AdminRepository adminRepository;
    private final UserJobRepository userJobRepository;

    @Value("${app.migration.encrypts:false}")
    private boolean shouldEncrypt;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateData() {
        if (!shouldEncrypt) {
            log.info("Skipping data encryption migration");
            return;
        }

        log.info("Starting data encryption migration...");

        try {
            // User 데이터 암호화
            List<User> users = userRepository.findAll();
            for (User user : users) {
                boolean needsUpdate = false;

                if (user.getUserNameKr() != null && !user.getUserNameKr().startsWith("XOR:")) {
                    user.setUserNameKr(EncryptionUtil.encrypt(user.getUserNameKr()));
                    needsUpdate = true;
                }
                if (user.getUserNameEn() != null && !user.getUserNameEn().startsWith("XOR:")) {
                    user.setUserNameEn(EncryptionUtil.encrypt(user.getUserNameEn()));
                    needsUpdate = true;
                }
                if (user.getUserInherentNumber() != null && !user.getUserInherentNumber().startsWith("XOR:")) {
                    user.setUserInherentNumber(EncryptionUtil.encrypt(user.getUserInherentNumber()));
                    needsUpdate = true;
                }
                if (user.getUserEmail() != null && !user.getUserEmail().startsWith("XOR:")) {
                    user.setUserEmail(EncryptionUtil.encrypt(user.getUserEmail()));
                    needsUpdate = true;
                }
                if (user.getUserPhone() != null && !user.getUserPhone().startsWith("XOR:")) {
                    user.setUserPhone(EncryptionUtil.encrypt(user.getUserPhone()));
                    needsUpdate = true;
                }
                if (user.getUserAddr() != null && !user.getUserAddr().startsWith("XOR:")) {
                    user.setUserAddr(EncryptionUtil.encrypt(user.getUserAddr()));
                    needsUpdate = true;
                }
                if (user.getUserAddrDetail() != null && !user.getUserAddrDetail().startsWith("XOR:")) {
                    user.setUserAddrDetail(EncryptionUtil.encrypt(user.getUserAddrDetail()));
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    entityManager.merge(user);
                    entityManager.flush();
                    log.info("Encrypted user data for user ID: {}", user.getUserId());
                }
            }

            // Account 데이터 암호화
            List<Account> accounts = accountRepository.findAll();
            for (Account account : accounts) {
                boolean needsUpdate = false;

                if (account.getAccountNumber() != null && !account.getAccountNumber().startsWith("XOR:")) {
                    account.setAccountNumber(EncryptionUtil.encrypt(account.getAccountNumber()));
                    needsUpdate = true;
                }
                if (account.getPinNumber() != null && !account.getPinNumber().startsWith("XOR:")) {
                    account.setPinNumber(EncryptionUtil.encrypt(account.getPinNumber()));
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    entityManager.merge(account);
                    entityManager.flush();
                    log.info("Encrypted account data for account number: {}", account.getAccountNumber());
                }
            }

            // Admin 데이터 암호화
            List<Admin> admins = adminRepository.findAll();
            for (Admin admin : admins) {
                boolean needsUpdate = false;

                if (admin.getName() != null && !admin.getName().startsWith("XOR:")) {
                    admin.setName(EncryptionUtil.encrypt(admin.getName()));
                    needsUpdate = true;
                }
                if (admin.getEmail() != null && !admin.getEmail().startsWith("XOR:")) {
                    admin.setEmail(EncryptionUtil.encrypt(admin.getEmail()));
                    needsUpdate = true;
                }
                if (admin.getPhoneNumber() != null && !admin.getPhoneNumber().startsWith("XOR:")) {
                    admin.setPhoneNumber(EncryptionUtil.encrypt(admin.getPhoneNumber()));
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    entityManager.merge(admin);
                    entityManager.flush();
                    log.info("Encrypted admin data for admin ID: {}", admin.getAdminId());
                }
            }

            // UserJob 데이터 암호화
            List<UserJob> userJobs = userJobRepository.findAll();
            for (UserJob userJob : userJobs) {
                boolean needsUpdate = false;

                if (userJob.getJobName() != null && !userJob.getJobName().startsWith("XOR:")) {
                    userJob.setJobName(EncryptionUtil.encrypt(userJob.getJobName()));
                    needsUpdate = true;
                }
                if (userJob.getCompanyName() != null && !userJob.getCompanyName().startsWith("XOR:")) {
                    userJob.setCompanyName(EncryptionUtil.encrypt(userJob.getCompanyName()));
                    needsUpdate = true;
                }
                if (userJob.getCompanyAddr() != null && !userJob.getCompanyAddr().startsWith("XOR:")) {
                    userJob.setCompanyAddr(EncryptionUtil.encrypt(userJob.getCompanyAddr()));
                    needsUpdate = true;
                }
                if (userJob.getCompanyPhone() != null && !userJob.getCompanyPhone().startsWith("XOR:")) {
                    userJob.setCompanyPhone(EncryptionUtil.encrypt(userJob.getCompanyPhone()));
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    entityManager.merge(userJob);
                    entityManager.flush();
                    log.info("Encrypted user job data for user code: {}", userJob.getUserCode());
                }
            }

            entityManager.flush();
            log.info("Data encryption migration completed successfully");

        } catch (Exception e) {
            log.error("Error during data migration: ", e);
            throw e;
        }
    }
}