-- 旧 DbInitListener のDDLを移植。
-- 変更点: 金額は DOUBLE ではなく NUMERIC(15,2)、PKは GENERATED ALWAYS AS IDENTITY、
--         注文日は VARCHAR(20) ではなく DATE、order_item に外部キー制約を追加。
CREATE TABLE product (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    price NUMERIC(15, 2) NOT NULL,
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
    unit_price NUMERIC(15, 2) NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);
