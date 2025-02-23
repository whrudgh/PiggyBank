package sesac.intruders.piggybank.domain.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesac.intruders.piggybank.domain.file.model.FileEntity;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Integer> {
    List<FileEntity> findByUploadDateBefore(LocalDateTime time);
}
