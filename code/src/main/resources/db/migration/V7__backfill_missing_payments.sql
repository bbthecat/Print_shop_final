-- order ที่สร้างก่อนมีระบบสร้าง payment อัตโนมัติ จะยังไม่มีแถวใน payments
-- เติม payment แบบ UNPAID ให้ (ยกเว้น order ที่ยกเลิกแล้ว) เพื่อให้ Staff บันทึกการชำระเงินจากหน้าเว็บได้
INSERT INTO payments (order_id, amount, payment_status, created_at)
SELECT o.id, o.total_price, 'UNPAID', CURRENT_TIMESTAMP
FROM print_orders o
WHERE o.status <> 'CANCELLED'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.order_id = o.id);
