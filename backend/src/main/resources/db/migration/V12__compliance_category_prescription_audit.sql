ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS requires_prescription BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS requires_strict_control BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS requires_verification BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGSERIAL PRIMARY KEY,
    patient_name VARCHAR(255) NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    issued_at DATE NOT NULL,
    valid_until DATE NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT false,
    verification_code VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE sales
    ADD COLUMN IF NOT EXISTS prescription_id BIGINT REFERENCES prescriptions(id);

CREATE INDEX IF NOT EXISTS idx_sales_prescription_id ON sales(prescription_id);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(80) NOT NULL,
    username VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
