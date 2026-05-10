package rw.bnr.backend_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.bnr.backend_api.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
