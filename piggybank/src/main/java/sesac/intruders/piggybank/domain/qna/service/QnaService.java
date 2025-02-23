package sesac.intruders.piggybank.domain.qna.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sesac.intruders.piggybank.domain.qna.dto.QnaRequest;
import sesac.intruders.piggybank.domain.qna.dto.QnaResponse;
import sesac.intruders.piggybank.domain.qna.model.Qna;
import sesac.intruders.piggybank.domain.qna.repository.QnaRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QnaService {
    @Autowired
    private QnaRepository qnaRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public List<QnaResponse> getAllQna() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return qnaRepository.findAll().stream()
                .map(qna -> new QnaResponse(
                        qna.getId(),
                        qna.getTitle(),
                        qna.getAuthor(),
                        qna.getContent(),
                        qna.getCreatedAt().format(formatter),
                        qna.isImportant()
                ))
                .collect(Collectors.toList());
    }

    public QnaResponse getQnaDtoById(Long id) {
        Qna qna = qnaRepository.findById(id).orElseThrow(() -> new RuntimeException("Qna not found"));
        return new QnaResponse(
                qna.getId(),
                qna.getTitle(),
                qna.getAuthor(),
                qna.getContent(),
                qna.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                qna.isImportant()
        );
    }

    public List<QnaResponse> searchQnaByTitle(String title) {
        // SQL Injection에 취약한 직접 쿼리 실행
        String sql;
        if (title == null || title.trim().isEmpty()) {
            sql = "SELECT * FROM qna";  // 빈 검색어일 때 모든 공지사항 반환
        } else {
            sql = "SELECT * FROM qna WHERE title LIKE '%" + title + "%'";  // % 와일드카드 추가
        }

        List<Qna> qna = entityManager.createNativeQuery(sql, Qna.class).getResultList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return qna.stream()
                .map(t -> new QnaResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getAuthor(),
                        t.getContent(),
                        t.getCreatedAt().format(formatter),
                        t.isImportant()
                ))
                .collect(Collectors.toList());
    }

    public void createQna(QnaRequest qnaRequest){
        LocalDateTime createdDate = LocalDateTime.now();

        qnaRepository.save(Qna.builder()
                .title(qnaRequest.getTitle())
                .author(qnaRequest.getAuthor())
                .content(qnaRequest.getContent())
                .important(qnaRequest.isImportant())
                .createdAt(createdDate)
                .updatedAt(createdDate)
                .build());
    }
}

