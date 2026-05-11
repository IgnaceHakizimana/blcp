package rw.bnr.backend_api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import rw.bnr.backend_api.model.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class DocumentResponse {
    private UUID id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer versionNumber;
    private Instant uploadedAt;

    public static DocumentResponse fromEntity(Document document) {
        return DocumentResponse.builder()
            .id(document.getId())
            .fileName(document.getFileName())
            .fileType(document.getFileType())
            .fileSize(document.getFileSize())
            .versionNumber(document.getVersionNumber())
            .uploadedAt(document.getUploadedAt())
            .build();
    }
}
