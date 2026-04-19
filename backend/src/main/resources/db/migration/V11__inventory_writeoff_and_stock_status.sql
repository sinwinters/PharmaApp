ALTER TABLE stock
    ADD COLUMN IF NOT EXISTS expiration_date DATE,
    ADD COLUMN IF NOT EXISTS reserved_quantity INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

UPDATE stock
SET expiration_date = COALESCE(expiration_date, DATE(expires_at), CURRENT_DATE + INTERVAL '90 days')
WHERE expiration_date IS NULL;

CREATE TABLE IF NOT EXISTS write_offs (
    id BIGSERIAL PRIMARY KEY,
    drug_id BIGINT NOT NULL REFERENCES drugs(id),
    quantity INTEGER NOT NULL,
    reason VARCHAR(30) NOT NULL,
    comment VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_write_offs_created_at ON write_offs(created_at);
CREATE INDEX IF NOT EXISTS idx_write_offs_reason ON write_offs(reason);
CREATE INDEX IF NOT EXISTS idx_write_offs_drug_id ON write_offs(drug_id);
