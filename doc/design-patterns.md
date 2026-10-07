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