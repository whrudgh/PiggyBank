package sesac.intruders.piggybank.domain.qna.dto;

public record QnaResponse(
        Long id,
        String title,
        String author,
        String content,
        String date,
        boolean important
) {
}

