ALTER TABLE drugs
    ADD COLUMN IF NOT EXISTS requires_eds_signature BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS eds_control_type VARCHAR(30);

UPDATE drugs d
SET requires_eds_signature = true,
    eds_control_type = 'ANTIBIOTIC'
FROM categories c
WHERE d.category_id = c.id
  AND c.name ILIKE '%антибиот%';

UPDATE drugs
SET requires_eds_signature = true,
    eds_control_type = 'NARCOTIC'
WHERE name ILIKE '%морфин%'
   OR name ILIKE '%фентан%'
   OR name ILIKE '%трамадол%';
