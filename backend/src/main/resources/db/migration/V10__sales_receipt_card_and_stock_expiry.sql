ALTER TABLE sales
    ADD COLUMN IF NOT EXISTS payment_type VARCHAR(20) NOT NULL DEFAULT 'CASH',
    ADD COLUMN IF NOT EXISTS medical_card_number VARCHAR(64),
    ADD COLUMN IF NOT EXISTS is_prescription_sale BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';

ALTER TABLE sale_items
    ADD COLUMN IF NOT EXISTS drug_name VARCHAR(300),
    ADD COLUMN IF NOT EXISTS line_total DECIMAL(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE stock
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;

UPDATE sale_items si
SET drug_name = d.name
FROM drugs d
WHERE si.drug_id = d.id
  AND si.drug_name IS NULL;

UPDATE sale_items
SET line_total = unit_price * quantity
WHERE line_total = 0;

UPDATE stock
SET expires_at = CURRENT_TIMESTAMP + INTERVAL '120 days'
WHERE expires_at IS NULL;

DO $$
DECLARE
    v_user_id BIGINT;
    v_drug_id BIGINT;
    v_sale1_id BIGINT;
    v_sale2_id BIGINT;
BEGIN
    SELECT id INTO v_user_id FROM users ORDER BY id LIMIT 1;
    SELECT id INTO v_drug_id FROM drugs ORDER BY id LIMIT 1;

    IF v_user_id IS NOT NULL AND v_drug_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sales) THEN
        INSERT INTO sales (user_id, total_amount, created_at, eds_required, eds_validated, payment_type, medical_card_number, is_prescription_sale, status)
        VALUES (v_user_id, 25.00, CURRENT_TIMESTAMP - INTERVAL '2 days', false, false, 'CASH', NULL, false, 'COMPLETED')
        RETURNING id INTO v_sale1_id;

        INSERT INTO sale_items (sale_id, drug_id, drug_name, quantity, unit_price, line_total)
        SELECT v_sale1_id, d.id, d.name, 1, 25.00, 25.00
        FROM drugs d WHERE d.id = v_drug_id;

        INSERT INTO sales (user_id, total_amount, created_at, eds_required, eds_validated, payment_type, medical_card_number, is_prescription_sale, status)
        VALUES (v_user_id, 20.00, CURRENT_TIMESTAMP - INTERVAL '1 days', false, false, 'CARD', 'MC-123456', true, 'COMPLETED')
        RETURNING id INTO v_sale2_id;

        INSERT INTO sale_items (sale_id, drug_id, drug_name, quantity, unit_price, line_total)
        SELECT v_sale2_id, d.id, d.name, 1, 20.00, 20.00
        FROM drugs d WHERE d.id = v_drug_id;
    END IF;
END $$;
