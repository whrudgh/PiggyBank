package sesac.intruders.piggybank.domain.qna.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesac.intruders.piggybank.domain.qna.dto.QnaRequest;
import sesac.intruders.piggybank.domain.qna.dto.QnaResponse;
import sesac.intruders.piggybank.domain.qna.service.QnaService;

import java.util.List;

@RestController
@RequestMapping("/qna")
public class QnaController {
    @Autowired
    private QnaService qnaService;

    // 전체 조회
    @GetMapping("")
    public List<QnaResponse> getAllPublicQna() {
        return qnaService.getAllQna();
    }

    // 상세조회
    @GetMapping("/{id}")
    public QnaResponse getPublicQnaById(@PathVariable Long id) {
        return qnaService.getQnaDtoById(id);
    }

    // 생성
    @PostMapping("")
    public ResponseEntity<Void> createQna(@RequestBody QnaRequest qnaRequest) {
        qnaService.createQna(qnaRequest);
        return ResponseEntity.ok().build();
    }

    // 검색
    @GetMapping("/search")
    public List<QnaResponse> searchPublicQna(@RequestParam String title) {
        return qnaService.searchQnaByTitle(title);
    }


}
