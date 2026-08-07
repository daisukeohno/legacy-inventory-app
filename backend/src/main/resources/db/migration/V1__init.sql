-- 旧 DbInitListener の DDL を PostgreSQL 向けに移植したスキーマ。
-- 金額は円整数前提のため NUMERIC(19,0)、AUTO_INCREMENT は IDENTITY 列に置き換える。
CREATE TABLE product (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku            VARCHAR(50)   NOT NULL,
    name           VARCHAR(200)  NOT NULL,
    price          NUMERIC(19,0) NOT NULL,
    stock_quantity INTEGER       NOT NULL,
    CONSTRAINT product_sku_uk UNIQUE (sku),
    CONSTRAINT product_price_positive CHECK (price >= 0),
    CONSTRAINT product_stock_positive CHECK (stock_quantity >= 0)
);

CREATE TABLE customer (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(200) NOT NULL,
    email VARCHAR(200)
);

-- orders は SQL 予約語だが、Flyway/JPA 双方で小文字非クォート識別子として統一して扱う。
CREATE TABLE orders (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    order_date    DATE         NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    CONSTRAINT orders_status_check CHECK (status IN ('NEW', 'SHIPPED'))
);

CREATE TABLE order_item (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT        NOT NULL,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(200)  NOT NULL,
    unit_price   NUMERIC(19,0) NOT NULL,
    quantity     INTEGER       NOT NULL,
    CONSTRAINT order_item_order_fk FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT order_item_product_fk FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT order_item_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX order_item_order_idx ON order_item (order_id);
