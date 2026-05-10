package rw.bnr.backend_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.User;
import rw.bnr.backend_api.model.enums.Role;
import rw.bnr.backend_api.security.CustomUserDetails;
import rw.bnr.backend_api.service.ApplicationService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
public class SecurityAuthorizationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    private final UUID appId = UUID.randomUUID();
    @Autowired
    private ApplicationController applicationController;
    @MockitoBean
    private ApplicationService applicationService;
    private CustomUserDetails applicantDetails;
    private CustomUserDetails reviewerDetails;
    private CustomUserDetails approverDetails;

    @BeforeEach
    void setUp() {
        applicantDetails = new CustomUserDetails(User.builder().role(Role.APPLICANT).build());
        reviewerDetails = new CustomUserDetails(User.builder().role(Role.REVIEWER).build());
        approverDetails = new CustomUserDetails(User.builder().role(Role.APPROVER).build());

        when(applicationService.submitApplication(any(), any())).thenReturn(new Application());
        when(applicationService.startReview(any(), any())).thenReturn(new Application());
        when(applicationService.approveApplication(any(), any())).thenReturn(new Application());
    }

    @Test
    @WithMockUser(roles = "APPLICANT")
    void applicant_CanSubmitApplication() {
        // Will succeed without throwing AccessDeniedException
        applicationController.submitApplication(appId, applicantDetails);
    }

    @Test
    @WithMockUser(roles = "APPLICANT")
    void applicant_CannotApproveApplication() {
        // Spring Security AOP intercepts and denies access
        assertThrows(AccessDeniedException.class, () -> {
            applicationController.approveApplication(appId, applicantDetails);
        });
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void reviewer_CanStartReview() {
        applicationController.startReview(appId, reviewerDetails);
    }

    @Test
    @WithMockUser(roles = "REVIEWER")
    void reviewer_CannotSubmitApplication() {
        assertThrows(AccessDeniedException.class, () -> {
            applicationController.submitApplication(appId, reviewerDetails);
        });
    }

    @Test
    @WithMockUser(roles = "APPROVER")
    void approver_CanApproveApplication() {
        applicationController.approveApplication(appId, approverDetails);
    }

    @Test
    @WithMockUser(roles = "APPROVER")
    void approver_CannotStartReview() {
        assertThrows(AccessDeniedException.class, () -> {
            applicationController.startReview(appId, approverDetails);
        });
    }
}
