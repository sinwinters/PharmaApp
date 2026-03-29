-- Дополнительные роли и пользователи, а также массовое наполнение справочников лекарств/остатков

-- 1. Роль MANAGER (если еще не существует)
INSERT INTO roles (name, description)
SELECT 'MANAGER', 'Менеджер аптеки'
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.name = 'MANAGER');

-- 2. Пользователь pharmacist / password (роль PHARMACIST)
INSERT INTO users (username, password_hash, email, role_id, enabled)
SELECT
    'pharmacist',
    -- BCrypt-хэш для пароля "password" (совместим с текущим PasswordEncoder)
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'pharmacist@pharma.local',
    r.id,
    true
FROM roles r
WHERE r.name = 'PHARMACIST'
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.username = 'pharmacist')
LIMIT 1;

-- 3. Пользователь manager / password (роль MANAGER)
INSERT INTO users (username, password_hash, email, role_id, enabled)
SELECT
    'manager',
    -- BCrypt-хэш для пароля "password"
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'manager@pharma.local',
    r.id,
    true
FROM roles r
WHERE r.name = 'MANAGER'
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.username = 'manager')
LIMIT 1;


-- 4. Массовое наполнение таблицы drugs (100 позиций) и соответствующих остатков в stock
-- Предполагаем, что базовые категории и поставщики уже созданы во V2__demo_data.sql

WITH cat AS (
    SELECT
        (SELECT id FROM categories WHERE name = 'Обезболивающие' ORDER BY id LIMIT 1)      AS analgesics_id,
        (SELECT id FROM categories WHERE name = 'Антибиотики' ORDER BY id LIMIT 1)         AS antibiotics_id,
        (SELECT id FROM categories WHERE name = 'Витамины' ORDER BY id LIMIT 1)            AS vitamins_id
),
sup AS (
    SELECT
        (SELECT id FROM suppliers WHERE name = 'ООО ФармСнаб' ORDER BY id LIMIT 1)         AS pharmsnab_id,
        (SELECT id FROM suppliers WHERE name = 'Аптека-Поставка' ORDER BY id LIMIT 1)      AS apteka_postavka_id
),
params AS (
    SELECT
        generate_series(1, 100) AS n,
        cat.analgesics_id,
        cat.antibiotics_id,
        cat.vitamins_id,
        sup.pharmsnab_id,
        sup.apteka_postavka_id
    FROM cat, sup
)
INSERT INTO drugs (name, category_id, supplier_id, min_quantity, unit, base_price)
SELECT
    CASE
        WHEN n % 3 = 1 THEN
            'Парацетамол ' || (n::text) || ' мг, табл. №10'
        WHEN n % 3 = 2 THEN
            'Ибупрофен ' || (n::text) || ' мг, табл. №20'
        ELSE
            'Аскорбиновая кислота ' || (n::text) || ' мг, шипучие табл.'
    END                                                          AS name,
    CASE
        WHEN n % 3 = 1 THEN analgesics_id
        WHEN n % 3 = 2 THEN analgesics_id
        ELSE vitamins_id
    END                                                          AS category_id,
    CASE
        WHEN n % 2 = 0 THEN pharmsnab_id
        ELSE apteka_postavka_id
    END                                                          AS supplier_id,
    CASE
        WHEN n % 4 = 0 THEN 20
        WHEN n % 4 = 1 THEN 30
        WHEN n % 4 = 2 THEN 50
        ELSE 10
    END                                                          AS min_quantity,
    'уп'                                                         AS unit,
    CASE
        WHEN n % 3 = 1 THEN 80.00 + (n % 10) * 5
        WHEN n % 3 = 2 THEN 120.00 + (n % 10) * 7
        ELSE 150.00 + (n % 10) * 4
    END::DECIMAL(12,2)                                           AS base_price
FROM params
WHERE analgesics_id IS NOT NULL
  AND antibiotics_id IS NOT NULL
  AND vitamins_id IS NOT NULL
  AND pharmsnab_id IS NOT NULL
  AND apteka_postavka_id IS NOT NULL;


-- 5. Инициализация остатков по только что созданным лекарствам
-- Для простоты: если у лекарства еще нет записи в stock, создаем ее
INSERT INTO stock (drug_id, quantity)
SELECT d.id,
       CASE
           WHEN d.name ILIKE 'Парацетамол%' THEN 200
           WHEN d.name ILIKE 'Ибупрофен%' THEN 150
           ELSE 120
       END AS quantity
FROM drugs d
LEFT JOIN stock s ON s.drug_id = d.id
WHERE s.id IS NULL;

