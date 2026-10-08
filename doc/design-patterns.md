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

---

## State (P4)

### ปัญหาที่แก้
Order มี 6 สถานะ แต่ละสถานะอนุญาต action ต่างกัน
ถ้าตรวจด้วย `if (order.getStatus() == ...)` กฎจะกระจายไปทุก method ที่แตะสถานะ
พอเพิ่มสถานะใหม่ต้องไล่หาแก้ทุกจุด และลืมจุดใดจุดหนึ่งได้ง่าย

### แนวทาง
`OrderState` เป็น interface ที่มี 5 action: `confirm()`, `process()`, `ready()`, `complete()`, `cancel()`
แต่ละ action คืนค่า **สถานะถัดไป**

`AbstractOrderState` ตั้งค่าเริ่มต้นให้ทุก action โยน `InvalidStateTransitionException`
คลาสของแต่ละสถานะ override เฉพาะ action ที่ตัวเองอนุญาตเท่านั้น

### ตาราง State Transition
| สถานะปัจจุบัน | action ที่อนุญาต | สถานะถัดไป | ผู้กระทำ |
|---|---|---|---|
| PENDING | confirm | CONFIRMED | Staff |
| PENDING | cancel | CANCELLED | Staff / Customer |
| CONFIRMED | process | PROCESSING | Staff |
| CONFIRMED | cancel | CANCELLED | Staff |
| PROCESSING | ready | READY | Staff |
| READY | complete | COMPLETED | Staff |
| COMPLETED | — | — (สถานะสุดท้าย) | — |
| CANCELLED | — | — (สถานะสุดท้าย) | — |

action อื่นนอกจากนี้ทั้งหมดถูกปฏิเสธ รวมถึงการยกเลิกหลังเริ่มพิมพ์แล้ว
(PROCESSING เป็นต้นไปยกเลิกไม่ได้ เพราะใช้กระดาษและหมึกไปแล้ว)

### ไฟล์/คลาสที่ใช้
| ไฟล์ | หน้าที่ |
|---|---|
| `service/state/OrderState.java` | interface กำหนด 5 action |
| `service/state/AbstractOrderState.java` | ค่าตั้งต้น = ปฏิเสธทุก action + สร้างข้อความ error |
| `service/state/PendingState.java` | อนุญาต confirm, cancel |
| `service/state/ConfirmedState.java` | อนุญาต process, cancel |
| `service/state/ProcessingState.java` | อนุญาต ready |
| `service/state/ReadyState.java` | อนุญาต complete |
| `service/state/CompletedState.java` | สถานะสุดท้าย |
| `service/state/CancelledState.java` | สถานะสุดท้าย |
| `service/state/OrderStateResolver.java` | แปลง OrderStatus เป็น OrderState ด้วย Map (ไม่ใช้ switch) |

### ผลลัพธ์เมื่อ transition ผิดกฎ
โยน `InvalidStateTransitionException` → `GlobalExceptionHandler` ตอบ **HTTP 409 Conflict**

ข้อความ error: `Cannot change status from COMPLETED to PROCESSING`

ครอบคลุมด้วย `OrderStateTest` ทั้ง transition ที่อนุญาตและที่ต้องถูกปฏิเสธ

### SOLID ที่เกี่ยวข้อง
- **OCP** — เพิ่มสถานะใหม่ = เพิ่มคลาส + ลงทะเบียนใน Resolver ไม่ต้องแก้คลาสสถานะเดิม
- **LSP** — ทุก state คืนค่า `OrderStatus` ตามสัญญาเดียวกัน การปฏิเสธใช้ exception ของ domain ที่ประกาศไว้ ไม่ใช่ `UnsupportedOperationException`

---

## Observer (P4)

### ปัญหาที่แก้
ทุกครั้งที่สถานะเปลี่ยน มีงานตามหลังหลายอย่าง: บันทึกประวัติ และแจ้งเตือนลูกค้า
ถ้าเขียนรวมใน `OrderStatusService` คลาสเดียวจะรับผิดชอบหลายเรื่อง
และทุกครั้งที่เพิ่มงานตามหลังใหม่ต้องกลับมาแก้ service เดิม

### แนวทาง
ใช้ `ApplicationEventPublisher` ของ Spring

OrderStatusService เปลี่ยนสถานะเสร็จ → publish `OrderStatusChangedEvent`
→ Listener ที่สนใจทำงานของตัวเองแยกกัน โดย service ไม่รู้จัก listener เลย

### สัญญาของ Event (Event Contract)
```java
OrderStatusChangedEvent(
    Long orderId,
    OrderStatus oldStatus,
    OrderStatus newStatus,
    Long changedBy      // user id ของคนที่กดเปลี่ยนสถานะ
)
```

### Listener
| Listener | ทำอะไร | เขียนลงตาราง |
|---|---|---|
| `OrderHistoryListener` | บันทึกว่าใครเปลี่ยนจากสถานะไหนเป็นสถานะไหน เมื่อไหร่ | `order_status_histories` |
| `InAppNotificationListener` | สร้างการแจ้งเตือนในระบบให้เจ้าของคำสั่งซื้อ | `notifications` |

### ผลลัพธ์
เพิ่มช่องทางแจ้งเตือนในอนาคต (Email / LINE) = เพิ่ม Listener ใหม่หนึ่งคลาส
ไม่ต้องแตะ `OrderStatusService` เลย

### SOLID ที่เกี่ยวข้อง
- **SRP** — listener แต่ละตัวทำงานเดียว
- **OCP** — เพิ่ม side-effect ใหม่โดยไม่แก้ publisher
