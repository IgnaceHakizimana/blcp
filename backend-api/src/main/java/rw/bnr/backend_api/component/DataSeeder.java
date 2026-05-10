package rw.bnr.backend_api.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.User;
import rw.bnr.backend_api.model.enums.ApplicationStatus;
import rw.bnr.backend_api.model.enums.Role;
import rw.bnr.backend_api.repository.ApplicationRepository;
import rw.bnr.backend_api.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User applicant = User.builder()
                .email("applicant@example.com")
                .passwordHash(passwordEncoder.encode("test123"))
                .role(Role.APPLICANT)
                .build();

            User reviewer = User.builder()
                .email("reviewer@example.com")
                .passwordHash(passwordEncoder.encode("test123"))
                .role(Role.REVIEWER)
                .build();

            User approver = User.builder()
                .email("approver@example.com")
                .passwordHash(passwordEncoder.encode("test123"))
                .role(Role.APPROVER)
                .build();

            userRepository.save(applicant);
            userRepository.save(reviewer);
            userRepository.save(approver);

            Application app1 = new Application();
            app1.setApplicant(applicant);
            app1.setCompanyName("Ubwizigame Microfinance Ltd");
            app1.setStatus(ApplicationStatus.SUBMITTED);

            Application app2 = new Application();
            app2.setApplicant(applicant);
            app2.setCompanyName("Icyizere Bank");
            app2.setStatus(ApplicationStatus.UNDER_REVIEW);
            app2.setReviewer(reviewer);

            applicationRepository.save(app1);
            applicationRepository.save(app2);
        }
    }
}
