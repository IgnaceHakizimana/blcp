package rw.bnr.backend_api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import rw.bnr.backend_api.model.Application;
import rw.bnr.backend_api.model.enums.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ApplicationResponse {
    private UUID id;
    private String companyName;
    private ApplicationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer version;

    public static ApplicationResponse fromEntity(Application application) {
        return ApplicationResponse.builder()
            .id(application.getId())
            .companyName(application.getCompanyName())
            .status(application.getStatus())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .version(application.getVersion())
            .build();
    }
}
