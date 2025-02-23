package sesac.intruders.piggybank.domain.file.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 업로드된 파일 정보를 클라이언트에게 내려줄 때 사용하는 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private int fileId;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadDate;

   
}
