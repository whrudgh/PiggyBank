package sesac.intruders.piggybank.domain.notice.controller;

import sesac.intruders.piggybank.domain.notice.dto.NoticeDTO;
import sesac.intruders.piggybank.domain.notice.model.Notice;
import sesac.intruders.piggybank.domain.notice.repository.NoticeRepository;
import sesac.intruders.piggybank.domain.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @GetMapping("/notices")
    public List<NoticeDTO> getAllPublicNotices() {
        return noticeService.getAllNotices();
    }

    @GetMapping("/notices/{id}")
    public NoticeDTO getPublicNoticeById(@PathVariable Long id) {
        return noticeService.getNoticeDTOById(id);
    }

    @GetMapping("/notices/search")
    public List<NoticeDTO> searchPublicNotices(@RequestParam String title) {
        return noticeService.searchNoticesByTitle(title);
    }
}
