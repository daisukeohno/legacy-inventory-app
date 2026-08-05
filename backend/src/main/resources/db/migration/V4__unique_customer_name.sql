-- 得意先は名称で解決するため、同名重複があると紐付け先が非決定になり
-- findByName も破綻する。既存重複を最小 id に統合したうえで一意制約を課す。
UPDATE orders o
   SET customer_id = c.keep_id
  FROM (SELECT id, MIN(id) OVER (PARTITION BY name) AS keep_id FROM customer) c
 WHERE o.customer_id = c.id
   AND c.id <> c.keep_id;

DELETE FROM customer c
 WHERE c.id <> (SELECT MIN(c2.id) FROM customer c2 WHERE c2.name = c.name);

ALTER TABLE customer ADD CONSTRAINT uk_customer_name UNIQUE (name);
