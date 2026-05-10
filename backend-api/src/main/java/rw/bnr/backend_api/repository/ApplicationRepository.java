package rw.bnr.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.bnr.backend_api.model.Application;

import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
}
