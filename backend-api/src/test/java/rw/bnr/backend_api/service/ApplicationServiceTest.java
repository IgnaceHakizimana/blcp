package rw.bnr.backend_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.User;
import rw.bnr.backend_api.model.enums.ApplicationStatus;
import rw.bnr.backend_api.model.enums.Role;
import rw.bnr.backend_api.repository.ApplicationRepository;
import rw.bnr.backend_api.repository.AuditLogRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User reviewer;
    private User approver;
    private Application application;

    @BeforeEach
    void setUp() {
        reviewer = User.builder().id(UUID.randomUUID()).role(Role.REVIEWER).build();
        approver = User.builder().id(UUID.randomUUID()).role(Role.APPROVER).build();
        application = new Application();
        application.setId(UUID.randomUUID());
    }

    @Test
    void testValidTransition_SubmitApplication() {
        application.setStatus(ApplicationStatus.DRAFT);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any())).thenReturn(application);

        Application updated = applicationService.submitApplication(application.getId(), reviewer);

        assertEquals(ApplicationStatus.SUBMITTED, updated.getStatus());
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void testInvalidTransition_ThrowsIllegalStateException() {
        application.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThrows(IllegalStateException.class, () -> {
            applicationService.submitApplication(application.getId(), reviewer);
        });
    }

    @Test
    void testHardRule_ReviewerCannotBeApprover() {
        application.setStatus(ApplicationStatus.PENDING_APPROVAL);
        application.setReviewer(reviewer);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        assertThrows(AccessDeniedException.class, () -> {
            applicationService.approveApplication(application.getId(), reviewer);
        });

        when(applicationRepository.save(any())).thenReturn(application);
        Application updated = applicationService.approveApplication(application.getId(), approver);
        assertEquals(ApplicationStatus.APPROVED, updated.getStatus());
    }
}
