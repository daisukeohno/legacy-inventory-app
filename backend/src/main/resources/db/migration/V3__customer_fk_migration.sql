-- 自由記述の orders.customer_name を customer マスタへ名寄せし、FK 参照へ移行する。
ALTER TABLE orders ADD COLUMN customer_id BIGINT;

-- 1) 完全一致でバックフィル
UPDATE orders o
SET customer_id = c.id
FROM customer c
WHERE o.customer_name = c.name;

-- 2) 未一致の得意先名は customer に新規作成して紐付ける
INSERT INTO customer (name, email)
SELECT DISTINCT o.customer_name, NULL
FROM orders o
WHERE o.customer_id IS NULL;

UPDATE orders o
SET customer_id = c.id
FROM customer c
WHERE o.customer_id IS NULL AND o.customer_name = c.name;

-- 3) NOT NULL 化 + FK 制約
ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT orders_customer_fk FOREIGN KEY (customer_id) REFERENCES customer (id);
CREATE INDEX orders_customer_idx ON orders (customer_id);

-- customer_name は互換のため非正規化コピーとして残す（将来削除予定）。
