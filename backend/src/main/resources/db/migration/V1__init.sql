CREATE TABLE product (
    id             BIGSERIAL PRIMARY KEY,
    sku            VARCHAR(50)   NOT NULL,
    name           VARCHAR(200)  NOT NULL,
    price          NUMERIC(19,0) NOT NULL,
    stock_quantity INTEGER       NOT NULL
);

CREATE TABLE customer (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(200) NOT NULL,
    email VARCHAR(200)
);

CREATE TABLE orders (
    id            BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    order_date    DATE         NOT NULL,
    status        VARCHAR(20)  NOT NULL CONSTRAINT orders_status_check CHECK (status IN ('NEW', 'SHIPPED'))
);

CREATE TABLE order_item (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders (id),
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(200)  NOT NULL,
    unit_price   NUMERIC(19,0) NOT NULL,
    quantity     INTEGER       NOT NULL
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);
