ALTER TABLE suppliers
    ADD COLUMN IF NOT EXISTS warehouse_gln VARCHAR(13);

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS destination_gln VARCHAR(13),
    ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(64),
    ADD COLUMN IF NOT EXISTS invoice_generated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS auto_order BOOLEAN NOT NULL DEFAULT false;

UPDATE suppliers
SET warehouse_gln = CASE name
    WHEN 'ООО ФармСнаб' THEN '4811234567890'
    WHEN 'Аптека-Поставка' THEN '4811234567891'
    ELSE warehouse_gln
END
WHERE warehouse_gln IS NULL;
