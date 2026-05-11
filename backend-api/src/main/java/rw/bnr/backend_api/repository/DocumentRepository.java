package rw.bnr.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.bnr.backend_api.model.Document;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByApplicationIdAndFileNameOrderByVersionNumberDesc(UUID applicationId, String fileName);
    List<Document> findByApplicationIdOrderByUploadedAtDesc(UUID applicationId);
    long countByApplicationId(UUID applicationId);
}
