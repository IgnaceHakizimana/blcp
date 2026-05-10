package rw.bnr.backend_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.bnr.backend_api.security.CustomUserDetails;
import rw.bnr.backend_api.service.DocumentService;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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
}
