package sesac.intruders.piggybank.domain.qna.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QnaRequest {
    private String title;
    private String author;
    private String content;
    private boolean important;
}
