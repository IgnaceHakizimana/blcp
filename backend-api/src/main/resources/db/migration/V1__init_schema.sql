CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL
);

CREATE TABLE applications
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_id UUID NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    reviewer_id UUID,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version      INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT fk_applicant FOREIGN KEY (applicant_id) REFERENCES users (id),
    CONSTRAINT fk_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id)
);

CREATE TABLE documents
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    uploader_id UUID NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    file_type      VARCHAR(100) NOT NULL,
    file_size      BIGINT       NOT NULL,
    storage_path   VARCHAR(500) NOT NULL,
    version_number INTEGER      NOT NULL,
    uploaded_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_document_application FOREIGN KEY (application_id) REFERENCES applications (id),
    CONSTRAINT fk_document_uploader FOREIGN KEY (uploader_id) REFERENCES users (id)
);

CREATE TABLE audit_logs
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    action          VARCHAR(100) NOT NULL,
    previous_status VARCHAR(50),
    new_status      VARCHAR(50),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_audit_application FOREIGN KEY (application_id) REFERENCES applications (id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE
OR
REPLACE FUNCTION prevent_audit_log_modification()
RETURNS TRIGGER AS $$
BEGIN RAISE EXCEPTION 'Audit logs are append-only. UPDATE or DELETE operations are strictly forbidden.';
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_append_only_audit_logs
    BEFORE UPDATE OR DELETE ON audit_logs
    FOR EACH ROW
EXECUTE FUNCTION prevent_audit_log_modification();
