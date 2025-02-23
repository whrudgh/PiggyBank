package sesac.intruders.piggybank.domain.file.dto.request;

import javax.validation.constraints.NotBlank;

/**
 * 업로드 시 필요한 메타데이터(예: userCode 등)를 담는 Request DTO.
 * 파일 자체(MultipartFile)는 Controller 메서드 파라미터(@RequestParam)로 받음.
 */
public class FileUploadRequest {

    @NotBlank(message = "userCode는 필수 입력 값입니다.")
    private String userCode;

    // 필요하다면, 업로드 목적이나 카테고리 등 추가 필드를 여기에 정의
    // ex) private String description;

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}
