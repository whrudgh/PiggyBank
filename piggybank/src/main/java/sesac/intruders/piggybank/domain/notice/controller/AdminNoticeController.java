package sesac.intruders.piggybank.domain.notice.controller;

import sesac.intruders.piggybank.domain.notice.dto.NoticeDTO;
import sesac.intruders.piggybank.domain.notice.model.Notice;
import sesac.intruders.piggybank.domain.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin/notices")
public class AdminNoticeController {
    @Autowired
    private NoticeService noticeService;

    @GetMapping("/search")
    public List<NoticeDTO> searchNotices(@RequestParam String title) {
        return noticeService.searchNoticesByTitle(title);
    }

    @PostMapping
    public NoticeDTO createNotice(@RequestBody Notice notice) {
        Notice createdNotice = noticeService.createNotice(notice);
        return new NoticeDTO(
                createdNotice.getId(),
                createdNotice.getTitle(),
                createdNotice.getContent(),
                createdNotice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                createdNotice.isImportant()
        );
    }

    @GetMapping
    public List<NoticeDTO> getAllNotices() {
        return noticeService.getAllNotices();
    }

    @GetMapping("/{id}")
    public NoticeDTO getNoticeById(@PathVariable Long id) {
        return noticeService.getNoticeDTOById(id);
    }

    @PutMapping("/{id}")
    public Notice updateNotice(@PathVariable Long id, @RequestBody Notice updatedNotice) {
        return noticeService.updateNotice(id, updatedNotice);
    }

    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
    }
} 