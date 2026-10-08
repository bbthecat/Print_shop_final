# Design Patterns

## Strategy (P2)

### ปัญหาที่แก้
ราคางานพิมพ์คำนวณต่างกันตามประเภท ถ้าใช้ if-else ก้อนเดียว
ทุกครั้งที่เพิ่มประเภทใหม่ต้องแก้โค้ดเดิม (ผิด Open/Closed)

### Pricing Strategy
| PricingType | สูตรคำนวณ | ตัวอย่าง |
|---|---|---|
| BLACK_WHITE | `(basePrice × pageCount) × copyCount` | ขาวดำ 1.50 บาท/หน้า, 20 หน้า, 2 ชุด = (1.50 × 20) × 2 = 60.00 บาท |
| COLOR | `(basePrice × pageCount) × copyCount` | พิมพ์สี 5.00 บาท/หน้า, 10 หน้า, 3 ชุด = (5.00 × 10) × 3 = 150.00 บาท |
| PHOTO | `(basePrice × pageCount) × copyCount` (กระดาษโฟโต้คุณภาพสูง) | พิมพ์ภาพถ่าย 15.00 บาท/หน้า, 5 หน้า, 1 ชุด = (15.00 × 5) × 1 = 75.00 บาท |

### บริการเสริม (Addon)
คิดราคาตามประเภทบริการเสริมต่อชุด หรือต่อแผ่น:
| บริการเสริม (Addon) | ราคา (ตัวอย่าง) | หน่วยคิดราคา |
|---|---|---|
| เย็บมุม (Corner Staple) | 2.00 บาท | ต่อชุด (copy) |
| เข้าเล่มสันเกลียว (Spiral Binding) | 25.00 บาท | ต่อเล่ม/ชุด (copy) |
| เข้าเล่มปกแข็ง (Hardcover Binding) | 80.00 บาท | ต่อเล่ม/ชุด (copy) |
| เคลือบพลาสติก (Lamination) | 10.00 บาท | ต่อหน้า/แผ่น (page × copy) |

*สูตรรวมใน PricingCalculator:*  
`Item Total = PrintPrice + Sum(AddonPrice)` (คืนค่า >= 0 เสมอ)

### กฎที่ต้องรักษา
- ทุก Strategy คืนค่า >= 0 เสมอ (ไม่ติดลบ)
- ห้าม throw UnsupportedOperationException (ปฏิบัติตาม Liskov Substitution Principle: LSP)
- เลือกใช้ Map-based `PricingStrategyResolver` เพื่อรองรับการเพิ่ม Strategy ใหม่โดยไม่ต้องแก้ไขโค้ดเดิม (Open/Closed Principle: OCP)

### ส่วนลด (Discount Strategy)
แยกประเภทส่วนลดตามประเภทโปรโมชัน (Percentage / FixedAmount)

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