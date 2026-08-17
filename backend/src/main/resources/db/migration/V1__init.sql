-- 旧 DbInitListener の DDL 直書きを Flyway へ移設したもの（PostgreSQL 方言）。
-- 旧 H2 スキーマとの対応:
--   INT AUTO_INCREMENT PRIMARY KEY -> BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
--   DOUBLE (金額)                  -> NUMERIC(12, 0)
--   order_date VARCHAR(20)         -> 現状の挙動維持のため VARCHAR(20) のまま
--     TODO: 将来的に DATE 型へ移行する（API/画面のフォーマット方針とあわせて実施）

CREATE TABLE product (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku            VARCHAR(50)   NOT NULL,
    name           VARCHAR(200)  NOT NULL,
    price          NUMERIC(12,0) NOT NULL,
    stock_quantity INTEGER       NOT NULL
);

CREATE TABLE customer (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(200) NOT NULL,
    email VARCHAR(200)
);

CREATE TABLE orders (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    order_date    VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL
);

CREATE TABLE order_item (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders (id),
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(200)  NOT NULL,
    unit_price   NUMERIC(12,0) NOT NULL,
    quantity     INTEGER       NOT NULL,
    item_index   INTEGER
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);
