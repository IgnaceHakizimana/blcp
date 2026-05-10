package rw.bnr.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.bnr.backend_api.model.AuditLog;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
