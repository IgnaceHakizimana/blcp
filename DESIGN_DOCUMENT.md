# Bank Licensing & Compliance Portal - Design Document

## 1. Architecture
**Backend Stack:** Java, Spring Boot, PostgreSQL, Flyway.
**Frontend Stack:** React, TypeScript, Vite, Tailwind CSS.

**Structure:** A monolithic, layered backend (Controllers -> Services -> Repositories).

**Reasoning:**
*   **The Monolith Backend:** The entire licensing process is a single, highly cohesive business domain. Splitting this into microservices would be unnecessary overengineering that introduces distributed transaction complexity without any real benefit. A monolithic architecture keeps the codebase simple, deployable, and ensures that state transitions and audit logs can be safely wrapped in a single local database transaction.
*   **Java & Spring Boot:** Selected for the backend due to their strong reliability, mature ecosystem, and widespread adoption in enterprise and financial systems. Features such as static typing and built-in transaction management make them well-suited for handling complex business workflows and maintaining data integrity in a regulatory environment.
*   **PostgreSQL:** A relational database was preferred because the system requires strict state management and ACID guarantees. PostgreSQL allowed to push critical security rules (like the append-only trigger for audit logs) directly down to the database level, ensuring data integrity regardless of the application state.
*   **React & TypeScript:** Chosen for the frontend to build strict, role-based views. TypeScript guarantees that our frontend strictly mirrors the Data Transfer Objects (DTOs) emitted by the backend, ensuring data correctness.
*   **Tailwind CSS:** Tailwind allows to rapidly build clean, tabular, internal-tool-style interfaces without the overhead of maintaining custom stylesheets or wrestling with heavy component libraries.

## 2. Data Model
*   **Users:** Central identity table.
*   **Applications:** The core entity holding the state machine. It contains foreign keys tracking the `applicant_id` and the `reviewer_id`, alongside a `version` column for concurrency control.
*   **Documents:** Immutable records tied to applications.
*   **Audit Logs:** A ledger recording all actor actions.

## 3. Roles & Permission Boundaries
A Role-Based Access Control (RBAC) system was implemented to enforce the Principle of Least Privilege:
*   **APPLICANT:** External users. Can submit applications and upload documents, but only when the application is in the `DRAFT` or `INFO_REQUESTED` state.
*   **REVIEWER:** Internal regulatory staff. Can view submitted applications, request more info, and recommend approval. They can review and recommend applications, but final approval decisions are handled only by APPROVER users.
*   **APPROVER:** Senior regulatory staff. Authorized only to make the final `APPROVED` or `REJECTED` decision.

## 4. The State Machine
Transitions are strictly enforced in the `@Transactional` service layer. Illegal transitions throw exceptions that map to `400 Bad Request`.
*   `DRAFT` -> `SUBMITTED` *(Applicant)*
*   `SUBMITTED` -> `UNDER_REVIEW` *(Reviewer)*
*   `UNDER_REVIEW` -> `INFO_REQUESTED` *(Reviewer)*
*   `INFO_REQUESTED` -> `SUBMITTED` *(Applicant)*
*   `UNDER_REVIEW` -> `PENDING_APPROVAL` *(Reviewer)*
*   `PENDING_APPROVAL` -> `APPROVED` | `REJECTED` *(Approver - Terminal States)*

## 5. The Hard Decisions (Trade-offs & Implementations)

### Authentication & Authorization (Session vs JWT)
*   **Implementation:** Stateful Session-based Authentication via Spring Security.
*   **Trade-off:** Stateless JWTs are popular for modern APIs, but in a high-security regulatory environment, administrators must be able to instantly revoke a compromised user's access. Stateless JWTs cannot be instantly revoked without building a complex stateful caching layer (which defeats the purpose of being stateless). Sessions provide instant revocation out-of-the-box at the cost of slight memory overhead.

### Handling Concurrent Access
*   **Implementation:** JPA Optimistic Locking (`@Version`).
*   **Trade-off:** Pessimistic locking (row-level database locks) guarantees conflict prevention, but it significantly reduces throughput and increases the risk of deadlocks. Optimistic locking was chosen instead because it supports higher read throughput and detects conflicts only at the moment a concurrent update occurs. When an `OptimisticLockingFailureException` is triggered, the application returns an HTTP `409 Conflict` response, aligning with standard REST semantics.

### Append-Only Audit Trail
*   **Implementation:** A PostgreSQL Database Trigger (`prevent_audit_log_modification`).
*   **Trade-off:** Application-level restrictions (e.g., just not writing a "delete" API endpoint) are insufficient against administrators or direct database access. By pushing the append-only rule down to a database trigger, it guarantees true ledger integrity. Any `UPDATE` or `DELETE` command executed against the `audit_logs` table throws a fatal database exception.

### Reviewer != Approver Rule
*   **Implementation:** Separation of Duties is enforced by persisting the `reviewer_id` during the `UNDER_REVIEW` transition. In the `approveApplication` method, a strict equality check (`reviewer.getId().equals(approver.getId())`) throws an `AccessDeniedException` if they match.

### API Documentation Strategy
*   **Implementation:** Automated OpenAPI documentation via SpringDoc / Swagger UI.
*   **Trade-off:** Providing a static Postman collection would have been easier to implement given the compatibility issues between the Spring Framework 7 and the SpringDoc libraries. However, maintaining a static Postman collection manually as an API evolves is tedious. I prioritized upgrading to the latest version of SpringDoc to resolve the issues, ensuring the API documentation automatically stays in sync with the source code.

## 6. Future Enhancements
*   **Strict Required Document Checklists:** Currently, the system simply requires the Bank name and at least one document to be uploaded for the applicant to submit an application. Given more time, I would implement a strict, required document checklist (e.g., *Proof of Capital, Board Resolution*). This would require a comprehensive backend validation, and specific upload slots on the frontend UI.
*   **On-Premise Object Storage (MinIO):** Rather than storing documents on the local application server disk, I would integrate an S3-compatible object storage solution like MinIO. Critically, because the Central Bank must adhere to strict data sovereignty and local data protection laws, public cloud solutions like AWS S3 are non-viable. MinIO allows for robust, scalable, on-premise document storage.
*   **Identity and Access Management (IAM):** I would migrate user authentication and role management out of the custom database tables and into a dedicated, enterprise-grade IAM solution like Keycloak for centralized identity brokering via OAuth2/OIDC.
*   **Multi-Factor Authentication (MFA):** Given the high-stakes financial nature of the portal, I would enforce strict MFA (via TOTP or USSD codes) for all internal regulators (`REVIEWER` and `APPROVER` roles). This provides a critical defense against compromised credentials being used to approve banking licenses.
*   **Workflow Engine:** I would extract the hardcoded State Machine into a dedicated framework like Spring State Machine to allow for visual graphing of the transitions and easier modifications to the workflow without touching core service logic.
