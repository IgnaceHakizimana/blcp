package rw.bnr.backend_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.AuditLog;
import rw.bnr.backend_api.model.User;
import rw.bnr.backend_api.model.enums.ApplicationStatus;
import rw.bnr.backend_api.model.enums.AuditAction;
import rw.bnr.backend_api.repository.ApplicationRepository;
import rw.bnr.backend_api.repository.AuditLogRepository;
import rw.bnr.backend_api.repository.DocumentRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AuditLogRepository auditLogRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public Application submitApplication(UUID applicationId, User currentUser) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.INFO_REQUESTED) {
            throw new IllegalStateException("Application can only be submitted from DRAFT or INFO_REQUESTED state.");
        }

        long documentCount = documentRepository.countByApplicationId(applicationId);
        if (documentCount == 0) {
            throw new IllegalStateException("An application must have at least one supporting document before it can be submitted.");
        }

        application.setComments(null);
        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.SUBMITTED);

        logAudit(application, currentUser, AuditAction.SUBMITTED, oldStatus, ApplicationStatus.SUBMITTED);
        return applicationRepository.save(application);
    }

    @Transactional
    public Application startReview(UUID applicationId, User reviewer) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED applications can be reviewed.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setReviewer(reviewer);

        logAudit(application, reviewer, AuditAction.REVIEW_STARTED, oldStatus, ApplicationStatus.UNDER_REVIEW);
        return applicationRepository.save(application);
    }

    @Transactional
    public Application requestInfo(UUID applicationId, User reviewer, String comments) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only applications UNDER_REVIEW can be sent back for info.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.INFO_REQUESTED);
        application.setComments(comments);

        logAudit(application, reviewer, AuditAction.INFO_REQUESTED, oldStatus, ApplicationStatus.INFO_REQUESTED);
        return applicationRepository.save(application);
    }

    @Transactional
    public Application recommendApproval(UUID applicationId, User reviewer) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only applications UNDER_REVIEW can be recommended for approval.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.PENDING_APPROVAL);
        application.setComments(null);

        logAudit(application, reviewer, AuditAction.RECOMMENDED_FOR_APPROVAL, oldStatus, ApplicationStatus.PENDING_APPROVAL);
        return applicationRepository.save(application);
    }

    @Transactional
    public Application approveApplication(UUID applicationId, User approver) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Application must be PENDING_APPROVAL to be approved.");
        }

        if (application.getReviewer() != null && application.getReviewer().getId().equals(approver.getId())) {
            throw new AccessDeniedException("The user who reviewed the application cannot make the final approval decision.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.APPROVED);
        application.setComments(null);

        logAudit(application, approver, AuditAction.APPROVED, oldStatus, ApplicationStatus.APPROVED);
        return applicationRepository.save(application);
    }

    @Transactional
    public Application rejectApplication(UUID applicationId, User approver, String comments) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Application must be PENDING_APPROVAL to be rejected.");
        }

        if (application.getReviewer() != null && application.getReviewer().getId().equals(approver.getId())) {
            throw new AccessDeniedException("The user who reviewed the application cannot make the final approval decision.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.REJECTED);
        application.setComments(comments);

        logAudit(application, approver, AuditAction.REJECTED, oldStatus, ApplicationStatus.REJECTED);
        return applicationRepository.save(application);
    }

    private Application getApplicationById(UUID id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    private void logAudit(Application application, User actor, AuditAction action, ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        AuditLog auditLog = new AuditLog();
        auditLog.setApplication(application);
        auditLog.setActor(actor);
        auditLog.setAction(action);
        auditLog.setPreviousStatus(oldStatus);
        auditLog.setNewStatus(newStatus);
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public java.util.List<Application> getApplicationsForApplicant(UUID applicantId) {
        return applicationRepository.findByApplicantId(applicantId);
    }

    @Transactional(readOnly = true)
    public java.util.List<Application> getAllNonDraftApplications() {
        return applicationRepository.findByStatusNot(ApplicationStatus.DRAFT);
    }

    @Transactional
    public Application createDraft(String companyName, User applicant) {
        Application application = new Application();
        application.setApplicant(applicant);
        application.setCompanyName(companyName);
        application.setStatus(ApplicationStatus.DRAFT);

        Application saved = applicationRepository.save(application);

        logAudit(saved, applicant, AuditAction.CREATED, null, ApplicationStatus.DRAFT);
        return saved;
    }
}
