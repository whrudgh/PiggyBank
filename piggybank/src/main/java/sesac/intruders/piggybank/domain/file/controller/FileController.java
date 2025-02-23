package sesac.intruders.piggybank.domain.file.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import sesac.intruders.piggybank.domain.file.dto.request.FileUploadRequest;
import sesac.intruders.piggybank.domain.file.dto.response.FileUploadResponse;
import sesac.intruders.piggybank.domain.file.service.FileService;

@RestController
@RequestMapping("/files")
@Validated
public class FileController {

    private final FileService fileService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ServletContext servletContext;

    public FileController(FileService fileService, HttpServletRequest request, HttpServletResponse response,
            ServletContext servletContext) {
        this.fileService = fileService;
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
    }

    /**
     * 단일 파일 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute @Valid FileUploadRequest request) {
        try {
            UUID userCode = UUID.fromString(request.getUserCode());
            FileUploadResponse response = fileService.saveFile(file, userCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    /**
     * 여러 파일 업로드
     */
    @PostMapping("/upload/multiple")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @ModelAttribute @Valid FileUploadRequest request) {
        try {
            UUID userCode = UUID.fromString(request.getUserCode());
            FileUploadResponse[] responses = fileService.saveFiles(files, userCode);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/download")
    public void handleFileRequest(@RequestParam("filePath") String encodedFilePath)
            throws IOException, ServletException {
        try {
            // 1. URL 디코딩
            String decodedFilePath = URLDecoder.decode(encodedFilePath, StandardCharsets.UTF_8);

            // 2. 경로 객체 생성
            Path filePath = Paths.get(decodedFilePath).toAbsolutePath().normalize();

            // 3. 파일 존재 여부 확인
            if (!Files.exists(filePath)) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "파일을 찾을 수 없습니다.");
                return;
            }

            String fileName = filePath.getFileName().toString();

            // 4. 일반 파일인 경우 다운로드
            Resource resource = new UrlResource(filePath.toUri());
            String mimeType = Files.probeContentType(filePath);
            mimeType = (mimeType != null) ? mimeType : "application/octet-stream";

            response.setContentType(mimeType);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            Files.copy(filePath, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "파일 처리 중 오류가 발생했습니다.");
        }
    }
}
