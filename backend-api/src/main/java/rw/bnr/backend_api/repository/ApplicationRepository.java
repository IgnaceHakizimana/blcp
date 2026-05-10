package rw.bnr.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.enums.ApplicationStatus;

import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    java.util.List<Application> findByApplicantId(UUID applicantId);
    java.util.List<Application> findByStatusNot(ApplicationStatus status);
}
