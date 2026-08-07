-- Replace the free-text orders.customer_name with a FK to customer.
ALTER TABLE orders ADD COLUMN customer_id BIGINT;

UPDATE orders o
SET customer_id = c.id
FROM customer c
WHERE c.name = o.customer_name;

-- Customers referenced only by free-text order rows are created (email unknown).
INSERT INTO customer (name, email)
SELECT DISTINCT o.customer_name, NULL
FROM orders o
WHERE o.customer_id IS NULL;

UPDATE orders o
SET customer_id = c.id
FROM customer c
WHERE o.customer_id IS NULL AND c.name = o.customer_name;

ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer (id);
ALTER TABLE orders DROP COLUMN customer_name;
