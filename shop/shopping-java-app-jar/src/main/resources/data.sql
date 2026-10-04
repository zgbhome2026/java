INSERT INTO products (sku, name, price, stock, active)
VALUES
    ('SKU-1001', 'Coffee Mug', 12.99, 50, TRUE),
    ('SKU-1002', 'T-Shirt', 24.99, 30, TRUE),
    ('SKU-1003', 'Backpack', 49.99, 20, TRUE)
ON CONFLICT (sku) DO NOTHING;
