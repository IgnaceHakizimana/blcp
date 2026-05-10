package rw.bnr.backend_api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.AuditLog;
import rw.bnr.backend_api.model.User;
import rw.bnr.backend_api.model.enums.ApplicationStatus;
import rw.bnr.backend_api.model.enums.AuditAction;
import rw.bnr.backend_api.model.enums.Role;
import rw.bnr.backend_api.repository.ApplicationRepository;
import rw.bnr.backend_api.repository.AuditLogRepository;
import rw.bnr.backend_api.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class DatabaseConstraintsIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    public void testConcurrentAccess_ThrowsOptimisticLockingException() {
        User applicant = userRepository.save(User.builder().email("test_conc@example.com").passwordHash("hash").role(Role.APPLICANT).build());
        Application app = new Application();
        app.setApplicant(applicant);
        app.setCompanyName("Concurrency Bank");
        app.setStatus(ApplicationStatus.DRAFT);
        app = applicationRepository.saveAndFlush(app);

        Application userAView = applicationRepository.findById(app.getId()).get();
        Application userBView = applicationRepository.findById(app.getId()).get();

        userAView.setStatus(ApplicationStatus.SUBMITTED);
        applicationRepository.saveAndFlush(userAView);

        userBView.setStatus(ApplicationStatus.UNDER_REVIEW);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            applicationRepository.saveAndFlush(userBView);
        });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testAuditLog_IsStrictlyAppendOnly() {
        User user = userRepository.save(User.builder().email("test_audit@example.com").passwordHash("hash").role(Role.APPLICANT).build());
        Application app = new Application();
        app.setApplicant(user);
        app.setCompanyName("Audit Bank");
        app = applicationRepository.saveAndFlush(app);

        AuditLog log = new AuditLog();
        log.setActor(user);
        log.setApplication(app);
        log.setAction(AuditAction.CREATED);
        log = auditLogRepository.saveAndFlush(log);

        AuditLog finalLog = log;
        assertThrows(Exception.class, () -> {
            auditLogRepository.delete(finalLog);
            auditLogRepository.flush();
        }, "The PostgreSQL trigger should reject the DELETE operation");
    }
}
