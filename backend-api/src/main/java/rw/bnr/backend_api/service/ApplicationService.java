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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public Application submitApplication(UUID applicationId, User currentUser) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.INFO_REQUESTED) {
            throw new IllegalStateException("Application can only be submitted from DRAFT or INFO_REQUESTED state.");
        }

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
    public Application recommendApproval(UUID applicationId, User reviewer) {
        Application application = getApplicationById(applicationId);

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only applications UNDER_REVIEW can be recommended for approval.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.PENDING_APPROVAL);

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

        logAudit(application, approver, AuditAction.APPROVED, oldStatus, ApplicationStatus.APPROVED);
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
}
