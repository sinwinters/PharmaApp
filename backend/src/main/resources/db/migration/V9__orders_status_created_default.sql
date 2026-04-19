UPDATE orders
SET status = 'CREATED'
WHERE status = 'DRAFT';

ALTER TABLE orders
    ALTER COLUMN status SET DEFAULT 'CREATED';
