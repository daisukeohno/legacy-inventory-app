CREATE TABLE product (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    price NUMERIC(19, 0) NOT NULL,
    stock_quantity INTEGER NOT NULL
);

CREATE TABLE customer (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(200)
);

CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    order_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE order_item (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders (id),
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price NUMERIC(19, 0) NOT NULL,
    quantity INTEGER NOT NULL
);

-- Seed data migrated from the legacy DbInitListener.
INSERT INTO product (sku, name, price, stock_quantity) VALUES
    ('SKU-1001', 'ノートPC 14インチ', 128000, 24),
    ('SKU-1002', 'ワイヤレスマウス', 2800, 6),
    ('SKU-1003', 'USB-Cハブ (7in1)', 4500, 3),
    ('SKU-1004', '外付けSSD 1TB', 15800, 40),
    ('SKU-1005', 'モニター 27インチ 4K', 46000, 8),
    ('SKU-1006', 'メカニカルキーボード', 9800, 15);

INSERT INTO customer (name, email) VALUES
    ('株式会社サンプル商事', 'order@sample-shoji.example.co.jp'),
    ('合同会社デモロジスティクス', 'contact@demo-logistics.example.co.jp');

INSERT INTO orders (customer_name, order_date, status) VALUES
    ('株式会社サンプル商事', DATE '2026-07-20', 'SHIPPED');

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity)
SELECT o.id, p.id, p.name, p.price, v.quantity
FROM orders o
CROSS JOIN (VALUES ('SKU-1001', 2), ('SKU-1002', 2)) AS v (sku, quantity)
JOIN product p ON p.sku = v.sku
WHERE o.customer_name = '株式会社サンプル商事';
