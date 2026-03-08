-- Демо-данные для тестирования (опционально, можно отключить в prod)
INSERT INTO users (username, password_hash, email, role_id)
SELECT 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@pharma.local', id
FROM roles
WHERE name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.username = 'admin')
LIMIT 1;

INSERT INTO users (username, password_hash, email, role_id)
SELECT 'demo', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'demo@pharma.local', id
FROM roles
WHERE name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.username = 'demo')
LIMIT 1;

INSERT INTO categories (name, description) VALUES
('Обезболивающие', 'Анальгетики и НПВП'),
('Антибиотики', 'Противомикробные препараты'),
('Витамины', 'Витамины и БАДы');

INSERT INTO suppliers (name, contact_info, email, phone) VALUES
('ООО ФармСнаб', 'Москва, ул. Складская 1', 'supply@pharmsnab.ru', '+7-495-111-22-33'),
('Аптека-Поставка', 'СПб, Невский 100', 'order@apteka-postavka.ru', '+7-812-333-44-55');

INSERT INTO drugs (name, category_id, supplier_id, min_quantity, unit, base_price)
SELECT 'Парацетамол 500мг', c.id, s.id, 50, 'уп', 85.00 FROM categories c, suppliers s WHERE c.name = 'Обезболивающие' AND s.name = 'ООО ФармСнаб' LIMIT 1;
INSERT INTO drugs (name, category_id, supplier_id, min_quantity, unit, base_price)
SELECT 'Ибупрофен 200мг', c.id, s.id, 30, 'уп', 120.00 FROM categories c, suppliers s WHERE c.name = 'Обезболивающие' AND s.name = 'ООО ФармСнаб' LIMIT 1;
INSERT INTO drugs (name, category_id, supplier_id, min_quantity, unit, base_price)
SELECT 'Амоксициллин 500мг', c.id, s.id, 20, 'уп', 250.00 FROM categories c, suppliers s WHERE c.name = 'Антибиотики' AND s.name = 'Аптека-Поставка' LIMIT 1;
INSERT INTO drugs (name, category_id, supplier_id, min_quantity, unit, base_price)
SELECT 'Витамин C 1000мг', c.id, s.id, 40, 'уп', 180.00 FROM categories c, suppliers s WHERE c.name = 'Витамины' AND s.name = 'ООО ФармСнаб' LIMIT 1;

INSERT INTO stock (drug_id, quantity)
SELECT id, 100 FROM drugs WHERE name = 'Парацетамол 500мг';
INSERT INTO stock (drug_id, quantity)
SELECT id, 45 FROM drugs WHERE name = 'Ибупрофен 200мг';
INSERT INTO stock (drug_id, quantity)
SELECT id, 15 FROM drugs WHERE name = 'Амоксициллин 500мг';
INSERT INTO stock (drug_id, quantity)
SELECT id, 60 FROM drugs WHERE name = 'Витамин C 1000мг';
