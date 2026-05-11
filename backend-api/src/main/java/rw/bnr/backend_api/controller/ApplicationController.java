package rw.bnr.backend_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.bnr.backend_api.dto.ApplicationResponse;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.enums.Role;
import rw.bnr.backend_api.security.CustomUserDetails;
import rw.bnr.backend_api.service.ApplicationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<ApplicationResponse> submitApplication(
        @PathVariable UUID id,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Application application = applicationService.submitApplication(id, userDetails.getUser());
        return ResponseEntity.ok(ApplicationResponse.fromEntity(application));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<ApplicationResponse> startReview(
        @PathVariable UUID id,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Application application = applicationService.startReview(id, userDetails.getUser());
        return ResponseEntity.ok(ApplicationResponse.fromEntity(application));
    }

    @PostMapping("/{id}/request-info")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<ApplicationResponse> requestInfo(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> payload,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        String comments = payload != null ? payload.get("comments") : null;
        Application application = applicationService.requestInfo(id, userDetails.getUser(), comments);
        return ResponseEntity.ok(ApplicationResponse.fromEntity(application));
    }

    @PostMapping("/{id}/recommend-approval")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<ApplicationResponse> recommendApproval(
        @PathVariable UUID id,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Application application = applicationService.recommendApproval(id, userDetails.getUser());
        return ResponseEntity.ok(ApplicationResponse.fromEntity(application));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('APPROVER')")
    public ResponseEntity<ApplicationResponse> approveApplication(
        @PathVariable UUID id,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Application application = applicationService.approveApplication(id, userDetails.getUser());
        return ResponseEntity.ok(ApplicationResponse.fromEntity(application));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('APPROVER')")
    public ResponseEntity<ApplicationResponse> rejectApplication(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> payload,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        String comments = payload != null ? payload.get("comments") : null;
        Application application = applicationService.rejectApplication(id, userDetails.getUser(), comments);
        return ResponseEntity.ok(ApplicationResponse.fromEntity(application));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplications(
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<Application> applications;

        if (userDetails.getUser().getRole() == Role.APPLICANT) {
            applications = applicationService.getApplicationsForApplicant(userDetails.getUser().getId());
        } else {
            applications = applicationService.getAllNonDraftApplications();
        }

        List<ApplicationResponse> response = applications.stream()
            .map(ApplicationResponse::fromEntity)
            .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<ApplicationResponse> createDraft(
        @RequestBody Map<String, String> payload,
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        String companyName = payload.get("companyName");
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }

        Application saved = applicationService.createDraft(companyName, userDetails.getUser());
        return ResponseEntity.ok(ApplicationResponse.fromEntity(saved));
    }
}
