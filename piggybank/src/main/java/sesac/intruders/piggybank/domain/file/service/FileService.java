package sesac.intruders.piggybank.domain.file.service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sesac.intruders.piggybank.domain.file.dto.response.FileUploadResponse;
import sesac.intruders.piggybank.domain.file.model.FileEntity;
import sesac.intruders.piggybank.domain.file.repository.FileRepository;
import sesac.intruders.piggybank.domain.user.model.User;
import sesac.intruders.piggybank.domain.user.repository.UserRepository;

@Service
public class FileService {

    @Value("${file.upload.base.dir}")
    private String baseUploadDir;

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileService(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    /**
     * 단일 파일 저장 로직
     */
    public FileUploadResponse saveFile(MultipartFile multipartFile, UUID userCode) throws IOException {
        // 1) User 정보 조회
        User user = userRepository.findById(userCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 2) 원본 파일명
        String originalFileName = multipartFile.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        /*
         * 3) 파일 확장자 확인
         * String[] allowedExtensions = {"jpg", "jpeg", "png", "pdf"};
         * String fileExtension =
         * originalFileName.substring(originalFileName.lastIndexOf(".") +
         * 1).toLowerCase();
         * if (!Arrays.asList(allowedExtensions).contains(fileExtension)) {
         * throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
         * }
         * 
         * // 4) MIME 타입 확인
         * Tika tika = new Tika();
         * String mimeType = tika.detect(multipartFile.getInputStream());
         * if (!isAllowedMimeType(mimeType)) {
         * throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
         * }
         */

        // 3) 사용자별 디렉토리 생성
        Path userDir = Paths.get(baseUploadDir, userCode.toString());
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // 4) 파일 저장 (타임스탬프 제거)
        Path filePath = userDir.resolve(originalFileName);

        // 5) 물리 파일 저장
        multipartFile.transferTo(filePath.toFile());

        // 6) DB 저장
        FileEntity fileEntity = new FileEntity();
        fileEntity.setUser(user);
        fileEntity.setFileName(originalFileName);
        fileEntity.setFilePath(filePath.toString());
        fileEntity.setUploadDate(LocalDateTime.now());

        FileEntity savedEntity = fileRepository.save(fileEntity);

        // 7) Response DTO로 변환하여 반환
        return new FileUploadResponse(
                savedEntity.getId(),
                savedEntity.getFileName(),
                savedEntity.getFilePath(),
                savedEntity.getUploadDate());
    }

    /**
     * 여러 개 파일 저장 로직
     */
    public FileUploadResponse[] saveFiles(MultipartFile[] multipartFiles, UUID userCode) throws IOException {
        FileUploadResponse[] responses = new FileUploadResponse[multipartFiles.length];

        for (int i = 0; i < multipartFiles.length; i++) {
            responses[i] = saveFile(multipartFiles[i], userCode);
        }

        return responses;
    }

    /**
     * MIME 타입 허용 여부 확인
     */
    private boolean isAllowedMimeType(String mimeType) {
        return mimeType.equals("image/jpeg") ||
                mimeType.equals("image/png") ||
                mimeType.equals("application/pdf");
    }

    /**
     * 일정 시간(30초) 후 파일 및 DB 정보 삭제 스케줄러
     */
    @Scheduled(fixedRate = 3000000) // 30초마다 실행
    public void deleteOldFiles() {
        // 🔧 1) DB에서 파일 정보 가져오기
        List<FileEntity> files = fileRepository.findAll();

        for (FileEntity fileEntity : files) {
            try {
                // 🔧 2) 업로드 시간이 30초 이상 경과했는지 확인
                if (fileEntity.getUploadDate().isBefore(LocalDateTime.now().minusSeconds(3000))) {
                    // 🔧 3) 로컬 파일 삭제
                    Path filePath = Paths.get(fileEntity.getFilePath());
                    Files.deleteIfExists(filePath);

                    // 🔧 4) 빈 디렉토리 삭제
                    Path userDir = filePath.getParent();
                    if (Files.exists(userDir) && isDirEmpty(userDir)) {
                        Files.delete(userDir);
                    }

                    // 🔧 5) DB에서 파일 정보 삭제
                    fileRepository.delete(fileEntity);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 디렉토리가 비어있는지 확인
     */
    private boolean isDirEmpty(Path path) throws IOException {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
            return !directory.iterator().hasNext();
        }
    }
}
