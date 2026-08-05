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
    ('株式会社サンプル商事', '2026-07-20', 'SHIPPED');

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity) VALUES
    (1, 1, 'ノートPC 14インチ', 128000, 2),
    (1, 2, 'ワイヤレスマウス', 2800, 2);
