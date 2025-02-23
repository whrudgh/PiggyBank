package sesac.intruders.piggybank.domain.notice.dto;

public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private String date;
    private boolean important;

    public NoticeDTO(Long id, String title, String content, String date, boolean important) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.important = important;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }
}
