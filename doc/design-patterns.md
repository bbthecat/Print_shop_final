# Design Patterns

## Strategy (P2)

### ปัญหาที่แก้
ราคางานพิมพ์คำนวณต่างกันตามประเภท ถ้าใช้ if-else ก้อนเดียว
ทุกครั้งที่เพิ่มประเภทใหม่ต้องแก้โค้ดเดิม (ผิด Open/Closed)

### Pricing Strategy
| PricingType | สูตรคำนวณ | ตัวอย่าง |
|---|---|---|
| BLACK_WHITE | | |
| COLOR | | |
| PHOTO | | |

### บริการเสริม (Addon)
-

### กฎที่ต้องรักษา
- ทุก Strategy คืนค่า >= 0
- ห้าม throw UnsupportedOperationException

### เพิ่ม section ส่วนลด (Percentage / FixedAmount)
-ทำส่วนลดแยกเป็นประเภท เช่น ส่วนลด % หรือ fixed ค่า

### Discount Strategy
| DiscountType | สูตร | ตัวอย่าง |
|---|---|---|
| PERCENTAGE | subtotal × (value / 100) | 10% ของ 200 = 20 |
| FIXED_AMOUNT | value | ลด 30 บาท |

### กฎโปรโมชัน
- ใช้ได้เมื่อ subtotal >= ยอดขั้นต่ำ (ไม่ถึง = ส่วนลด 0 หรือ error ตามที่ทีมตกลง)
- ใช้ได้เฉพาะช่วงวันที่เริ่ม-สิ้นสุด และ active = true
- ส่วนลดต้องไม่เกิน subtotal (ราคาสุทธิไม่ติดลบ)
- โค้ดโปรโมชันซ้ำกันไม่ได้ (UNIQUE)