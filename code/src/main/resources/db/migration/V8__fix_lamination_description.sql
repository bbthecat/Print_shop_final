-- PricingCalculator คิดราคางานเก็บเล่มทุกตัวเป็น "ต่อชุด" (ราคา × copyCount)
-- แต่ V5 เขียนคำอธิบาย Lamination ว่า per page ซึ่งไม่ตรงกับการคิดเงินจริง
-- แก้ผ่าน migration ใหม่แทนการแก้ V5 เพราะ V5 รันบนฐานข้อมูลจริงไปแล้ว (Flyway จะตรวจ checksum)
UPDATE addon_services
SET description = 'A4 hot pouch lamination per copy',
    updated_at = CURRENT_TIMESTAMP
WHERE name = 'Lamination'
  AND description = 'A4 hot pouch lamination per page';
