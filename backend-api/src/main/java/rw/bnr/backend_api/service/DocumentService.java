package rw.bnr.backend_api.service;

import java.nio.file.StandardCopyOption;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.Document;
import rw.bnr.backend_api.model.User;
import rw.bnr.backend_api.model.enums.ApplicationStatus;
import rw.bnr.backend_api.repository.ApplicationRepository;
import rw.bnr.backend_api.repository.DocumentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;

    private final String UPLOAD_DIR = "uploads/";

    @Transactional
    public Document uploadDocument(UUID applicationId, MultipartFile file, User uploader) throws IOException {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.INFO_REQUESTED) {
            throw new IllegalStateException("Documents can only be uploaded when the application is in DRAFT or INFO_REQUESTED state.");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        List<Document> existingDocs = documentRepository.findByApplicationIdAndFileNameOrderByVersionNumberDesc(applicationId, fileName);
        int version = existingDocs.isEmpty() ? 1 : existingDocs.get(0).getVersionNumber() + 1;

        String storageFileName = UUID.randomUUID() + "_" + fileName;
        Path filePath = uploadPath.resolve(storageFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document();
        document.setApplication(application);
        document.setUploader(uploader);
        document.setFileName(fileName);
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setStoragePath(filePath.toString());
        document.setVersionNumber(version);

        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByApplicationId(UUID applicationId) {
        return documentRepository.findByApplicationIdOrderByUploadedAtDesc(applicationId);
    }

    @Transactional(readOnly = true)
    public Resource getDocumentResource(UUID documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        Path filePath = Paths.get(document.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found on disk");
        }

        return new UrlResource(filePath.toUri());
    }

    @Transactional(readOnly = true)
    public Document getDocumentById(UUID documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }
}
