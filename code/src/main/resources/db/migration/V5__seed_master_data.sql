-- Seed Print Services
INSERT INTO print_services (name, description, base_price, pricing_type, active, created_at, updated_at)
VALUES 
('Document B&W (A4)', 'Standard black and white printing on 80gsm paper', 1.50, 'BLACK_WHITE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Document Color (A4)', 'High quality color printing on 80gsm paper', 5.00, 'COLOR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Photo Print (High Quality)', 'Glossy photo paper printing with premium colors', 15.00, 'PHOTO', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Addon Services
INSERT INTO addon_services (name, description, price, active, created_at, updated_at)
VALUES 
('Corner Staple', 'Staple at top-left corner per copy', 2.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Spiral Binding', 'Plastic spiral comb binding with clear plastic covers', 25.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hardcover Binding', 'Premium hardcover binding with gold foil lettering', 80.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lamination', 'A4 hot pouch lamination per page', 10.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Sample Promotions
INSERT INTO promotions (code, description, discount_type, discount_value, min_order_amount, start_date, end_date, active, created_at, updated_at)
VALUES 
('WELCOME10', '10% discount for orders over 100 THB', 'PERCENTAGE', 10.00, 100.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SAVE30', 'Fixed 30 THB discount for orders over 200 THB', 'FIXED_AMOUNT', 30.00, 200.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
