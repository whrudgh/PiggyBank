//5차 수정
package sesac.intruders.piggybank.domain.notice.service;

import sesac.intruders.piggybank.domain.admin.model.Admin;
import sesac.intruders.piggybank.domain.admin.repository.AdminRepository;
import sesac.intruders.piggybank.domain.notice.dto.NoticeDTO;
import sesac.intruders.piggybank.domain.notice.model.Notice;
import sesac.intruders.piggybank.domain.notice.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeService {
    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private AdminRepository adminRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Notice createNotice(Notice notice) {
        Admin admin = adminRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        notice.setAdmin(admin);
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        notice.setImportant(notice.isImportant());
        return noticeRepository.save(notice);
    }

    public List<NoticeDTO> searchNoticesByTitle(String title) {
        // SQL Injection에 취약한 직접 쿼리 실행
        String sql;
        if (title == null || title.trim().isEmpty()) {
            sql = "SELECT * FROM notices";  // 빈 검색어일 때 모든 공지사항 반환
        } else {
            sql = "SELECT * FROM notices WHERE title LIKE '%" + title + "%'";  // % 와일드카드 추가
        }
        
        List<Notice> notices = entityManager.createNativeQuery(sql, Notice.class).getResultList();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return notices.stream()
                .map(notice -> new NoticeDTO(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getContent(),
                        notice.getCreatedAt().format(formatter),
                        notice.isImportant()
                ))
                .collect(Collectors.toList());
    }

    public List<NoticeDTO> getAllNotices() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return noticeRepository.findAll().stream()
                .map(notice -> new NoticeDTO(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getContent(),
                        notice.getCreatedAt().format(formatter),
                        notice.isImportant()
                ))
                .collect(Collectors.toList());
    }

    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id).orElseThrow(() -> new RuntimeException("Notice not found"));
    }

    public NoticeDTO getNoticeDTOById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
        return new NoticeDTO(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                notice.isImportant()
        );
    }

    public Notice updateNotice(Long id, Notice updatedNotice) {
        Notice existingNotice = getNoticeById(id);
        existingNotice.setTitle(updatedNotice.getTitle());
        existingNotice.setContent(updatedNotice.getContent());
        existingNotice.setUpdatedAt(LocalDateTime.now());
        existingNotice.setImportant(updatedNotice.isImportant());
        return noticeRepository.save(existingNotice);
    }

    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}

