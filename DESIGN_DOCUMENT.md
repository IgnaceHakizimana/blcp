## 1. Architecture
**Stack:** Java, Spring Boot, PostgreSQL.

**Structure:** A monolithic, layered architecture (Controllers -> Services -> Repositories).

**Reasoning:**
*   **The Monolith:** The entire licensing process is a single, highly cohesive business domain. Splitting this into microservices would be unnecessary overengineering that introduces network latency and distributed transaction complexity without any real benefit. A monolithic architecture keeps the codebase simple, deployable, and ensures that state transitions and audit logs can be safely wrapped in a single local database transaction.
*   **Java & Spring Boot:** Chosen for the backend because it is the industry standard for enterprise financial software. Its strict static typing, robust `@Transactional` management, and built-in security modules provide a highly defensible foundation for a regulatory system.
*   **PostgreSQL:** Relational databases are mandatory for systems requiring strict state management and ACID guarantees. PostgreSQL was specifically chosen because of its advanced feature set, which allowed us to push critical security rules (like the append-only trigger for audit logs) directly down to the database level, ensuring data integrity regardless of the application state.


## 2. Data Model
*   **Users:** Central identity table.
*   **Applications:** The core entity holding the state machine. It contains foreign keys tracking the `applicant_id` and the `reviewer_id`, alongside a `version` column for concurrency control.
*   **Documents:** Immutable records tied to applications.
*   **Audit Logs:** A ledger recording all actor actions.

## 3. Roles & Permission Boundaries
We implemented a strict Role-Based Access Control (RBAC) system enforcing the Principle of Least Privilege:
*   **APPLICANT:** External users. Can submit applications and upload documents, but only when the application is in the `DRAFT` or `INFO_REQUESTED` state.
*   **REVIEWER:** Internal regulatory staff. Can view submitted applications, request more info, and recommend approval. They are explicitly walled off from final decisions.
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
*   **Trade-off:** We could have used Pessimistic Locking (row-level DB locks), which guarantees no conflicts but heavily degrades throughput and risks deadlocks. Optimistic locking allows high read throughput and gracefully rejects the transaction the exact millisecond a conflict occurs. We catch this `OptimisticLockingFailureException` and return an HTTP `409 Conflict`, which fits the REST paradigm perfectly.

### Append-Only Audit Trail
*   **Implementation:** A PostgreSQL Database Trigger (`prevent_audit_log_modification`).
*   **Trade-off:** Application-level restrictions (e.g., just not writing a "delete" API endpoint) are insufficient against administrators or direct database access. By pushing the append-only rule down to a database trigger, we guarantee true ledger integrity. Any `UPDATE` or `DELETE` command executed against the `audit_logs` table throws a fatal database exception.

### Reviewer != Approver Rule
*   **Implementation:** Separation of Duties is enforced by persisting the `reviewer_id` during the `UNDER_REVIEW` transition. In the `approveApplication` method, a strict equality check (`reviewer.getId().equals(approver.getId())`) throws an `AccessDeniedException` if they match.


### Given More Time
*   **On-Premise Object Storage (MinIO):** Rather than storing documents on the local application server disk, I would integrate an S3-compatible object storage solution like MinIO. Critically, because the Central Bank must adhere to strict data sovereignty and local data protection laws, public cloud solutions like AWS S3 are non-viable. MinIO allows for robust, scalable, on-premise document storage.
*   **Identity and Access Management (IAM):** I would migrate user authentication and role management out of the custom database tables and into a dedicated, enterprise-grade IAM solution like Keycloak for centralized identity brokering via OAuth2/OIDC.
*   **Multi-Factor Authentication (MFA):** Given the high-stakes financial nature of the portal, I would enforce strict MFA (via TOTP or hardware security keys) for all internal regulators (`REVIEWER` and `APPROVER` roles). This provides a critical defense against compromised credentials being used to approve banking licenses.
*   **Workflow Engine:** I would extract the hardcoded State Machine into a dedicated framework like Spring State Machine to allow for visual graphing of the transitions and easier modifications to the workflow without touching core service logic.
