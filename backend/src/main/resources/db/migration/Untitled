-- 3НФ: роли пользователей
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Пользователи (нормализация: роль вынесена)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role_id ON users(role_id);

-- Категории лекарств
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500)
);

-- Поставщики
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact_info VARCHAR(500),
    email VARCHAR(255),
    phone VARCHAR(50)
);

-- Лекарства (зависимости от категории и поставщика)
CREATE TABLE drugs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(300) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    min_quantity INTEGER NOT NULL DEFAULT 10,
    unit VARCHAR(50) NOT NULL DEFAULT 'шт',
    base_price DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_drugs_category_id ON drugs(category_id);
CREATE INDEX idx_drugs_supplier_id ON drugs(supplier_id);
CREATE INDEX idx_drugs_name ON drugs(name);

-- Остатки на складе
CREATE TABLE stock (
    id BIGSERIAL PRIMARY KEY,
    drug_id BIGINT NOT NULL REFERENCES drugs(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(drug_id)
);

CREATE INDEX idx_stock_drug_id ON stock(drug_id);

-- Продажи
CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    total_amount DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sales_user_id ON sales(user_id);
CREATE INDEX idx_sales_created_at ON sales(created_at);

-- Позиции продажи
CREATE TABLE sale_items (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    drug_id BIGINT NOT NULL REFERENCES drugs(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12, 2) NOT NULL
);

CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_drug_id ON sale_items(drug_id);

-- Заказы поставщикам
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_supplier_id ON orders(supplier_id);
CREATE INDEX idx_orders_status ON orders(status);

-- Позиции заказа
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    drug_id BIGINT NOT NULL REFERENCES drugs(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Начальные данные
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Администратор'), 
('PHARMACIST', 'Провизор'), 
('CASHIER', 'Кассир');

-- Пароль admin: "password" захеширован через BCrypt
-- $2a$10$Dow1U0mWgK7J2Yq3u9tYkO6F0kGkF8YvF6j7VxKZ6Q0r1p5hS9QxW
INSERT INTO users (username, password_hash, email, role_id, enabled)
VALUES (
    'admin', 
    '$2a$10$Dow1U0mWgK7J2Yq3u9tYkO6F0kGkF8YvF6j7VxKZ6Q0r1p5hS9QxW', 
    'admin@example.com', 
    (SELECT id FROM roles WHERE name='ADMIN'), 
    true
);