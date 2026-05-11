package rw.bnr.backend_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.bnr.backend_api.dto.DocumentResponse;
import rw.bnr.backend_api.model.Document;
import rw.bnr.backend_api.security.CustomUserDetails;
import rw.bnr.backend_api.service.DocumentService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications/{applicationId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<Map<String, String>> uploadDocument(
        @PathVariable UUID applicationId,
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        documentService.uploadDocument(applicationId, file, userDetails.getUser());
        return ResponseEntity.ok(Map.of("message", "Document uploaded successfully"));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable UUID applicationId) {
        List<Document> documents = documentService.getDocumentsByApplicationId(applicationId);
        List<DocumentResponse> response = documents.stream()
            .map(DocumentResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID documentId) throws IOException {
        Resource resource = documentService.getDocumentResource(documentId);
        Document document = documentService.getDocumentById(documentId);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.getFileType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
            .body(resource);
    }
}
