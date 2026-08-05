-- orders の自由記述 customer_name を customer テーブルへ正規化する。
ALTER TABLE orders ADD COLUMN customer_id BIGINT;

-- 1) 既存 customer と名称完全一致する行を紐付ける
UPDATE orders o
   SET customer_id = c.id
  FROM customer c
 WHERE c.name = o.customer_name
   AND o.customer_id IS NULL;

-- 2) 未一致の customer_name は customer に新規登録してから紐付ける
INSERT INTO customer (name, email)
SELECT DISTINCT o.customer_name, NULL
  FROM orders o
 WHERE o.customer_id IS NULL;

UPDATE orders o
   SET customer_id = c.id
  FROM customer c
 WHERE c.name = o.customer_name
   AND o.customer_id IS NULL;

-- 3) FK + NOT NULL 化
ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer (id);
CREATE INDEX idx_orders_customer_id ON orders (customer_id);

-- customer_name 列は監査・突合用に当面残置する。
-- 突合完了後に「ALTER TABLE orders DROP COLUMN customer_name;」を将来のマイグレーションで実施する想定。
