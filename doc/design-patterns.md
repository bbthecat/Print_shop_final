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

### ข้อความแจ้งเตือนรายสถานะ
Listener ใช้ตารางนี้สร้าง `title` และ `message` ลงตาราง `notifications`
โดยแทน `{orderNumber}` ด้วยเลขที่คำสั่งซื้อจริง

| สถานะใหม่ | title | message |
|---|---|---|
| CONFIRMED | ร้านรับงานแล้ว | คำสั่งซื้อ {orderNumber} ได้รับการยืนยันแล้ว ร้านจะเริ่มดำเนินการให้เร็วที่สุด |
| PROCESSING | กำลังดำเนินการ | คำสั่งซื้อ {orderNumber} กำลังพิมพ์อยู่ |
| READY | งานเสร็จแล้ว | คำสั่งซื้อ {orderNumber} พร้อมให้มารับที่ร้านแล้ว |
| COMPLETED | ส่งมอบงานแล้ว | คำสั่งซื้อ {orderNumber} ส่งมอบเรียบร้อย ขอบคุณที่ใช้บริการ |
| CANCELLED | คำสั่งซื้อถูกยกเลิก | คำสั่งซื้อ {orderNumber} ถูกยกเลิกแล้ว |

**กฎ**
- แจ้งเตือนส่งถึงเจ้าของคำสั่งซื้อ (`print_orders.user_id`) เท่านั้น
- สถานะ PENDING ไม่สร้างการแจ้งเตือน เพราะเป็นสถานะตั้งต้นตอนลูกค้าสร้างคำสั่งซื้อเอง
- `is_read` เริ่มต้นเป็น `false` เสมอ
- การสร้างแจ้งเตือนล้มเหลวต้องไม่ทำให้การเปลี่ยนสถานะล้มเหลวตาม

---

## Chain of Responsibility (P3)

### ปัญหาที่แก้
การตรวจสอบข้อมูลก่อนสร้างคำสั่งซื้อ (Order Validation) มีหลายขั้นตอนและหลายเงื่อนไข:
- บริการที่สั่งพิมพ์ยังเปิดให้บริการอยู่หรือไม่
- ประเภทไฟล์เอกสารตรงตามข้อกำหนดหรือไม่ (PDF, JPEG, PNG)
- จำนวนที่สั่งพิมพ์ถูกต้องหรือไม่ (ห้ามเป็นศูนย์หรือติดลบ)
- รหัสโปรโมชันที่ระบุถูกต้องและยังไม่หมดอายุหรือไม่

หากเขียนตรวจสอบทั้งหมดรวมกันใน `OrderCommandService` คลาสเดียวด้วย `if-else` ก้อนใหญ่:
- ฝ่าฝืน **Single Responsibility Principle (SRP)** เพราะ Service ต้องแบกรับตรรกะการตรวจสอบข้อมูลทุกประเภท
- ฝ่าฝืน **Open/Closed Principle (OCP)** เพราะทุกครั้งที่มีกฎการตรวจสอบใหม่ (เช่น เพิ่มการตรวจขนาดไฟล์ หรือความละเอียดภาพ) จะต้องกลับมาแก้ Service เดิม
- โค้ดยากต่อการทดสอบแยกส่วน (Unit Test)

### แนวทาง
ใช้ **Chain of Responsibility (GoF Behavioral Pattern)**:
1. กำหนด `OrderValidationHandler` เป็น Abstract Base Handler มีพอยน์เตอร์ `next` และเมธอด `handle(OrderValidationContext context)`
2. ห่อหุ้มข้อมูลคำสั่งซื้อที่ต้องใช้ตรวจสอบไว้ใน `OrderValidationContext`
3. แยกกฎการตรวจสอบแต่ละเรื่องออกเป็น Handler เฉพาะตัว 4 ตัว
4. หากการตรวจสอบใน Handler ตัวใดไม่ผ่าน จะโยน `ValidationException` ทันที และหยุดการทำงานของ Chain
5. หากผ่าน จะส่งต่อให้ Handler ถัดไปด้วย `next.handle(context)`
6. การประกอบสาย Chain ทำผ่าน Spring `@Configuration` (`OrderValidationChainConfig`) และ Inject เข้า `OrderCommandService` ผ่าน Constructor Injection

### ลำดับ Chain การตรวจสอบ (Validation Flow)

```
[Request] → ServiceAvailabilityHandler → FileTypeValidationHandler → QuantityValidationHandler → PromotionValidityHandler → [Save Order]
                     ↓                              ↓                          ↓                           ↓
            (โยน 400 ถ้าไม่พบ/ปิด)       (โยน 400 ถ้าไฟล์ผิด)         (โยน 400 ถ้า quantity<=0)   (โยน 400 ถ้าโปรโมชันหมดอายุ)
```

| ลำดับ | Handler | สิ่งที่ตรวจสอบ | ข้อยกเว้น/ข้อความ Error |
|---|---|---|---|
| 1 | `ServiceAvailabilityHandler` | ตรวจสอบว่า `serviceId` ของทุก Item มีอยู่จริงและ `active = true` ผ่าน `ServiceCatalogQueryService` | `ValidationException` / `ResourceNotFoundException` ("Service not found with ID: {id}") |
| 2 | `FileTypeValidationHandler` | ตรวจสอบ Mime Type ของไฟล์ที่แนบ รองรับเฉพาะ `application/pdf`, `image/jpeg`, `image/png` | `ValidationException` ("File type is not supported: {type}") |
| 3 | `QuantityValidationHandler` | ตรวจสอบว่าจำนวน `quantity` ของแต่ละ Item ต้องมากกว่า 0 | `ValidationException` ("Quantity must be greater than 0") |
| 4 | `PromotionValidityHandler` | ตรวจสอบโปรโมชัน (ถ้ามี) ว่ามีอยู่จริง, `active = true`, และวันเวลาปัจจุบันอยู่ในช่วง `startDate` ถึง `endDate` | `ValidationException` ("Promotion is inactive: {code}" หรือ "Promotion is not valid at this time: {code}") |

### ไฟล์/คลาสที่ใช้
| ไฟล์ | หน้าที่ |
|---|---|
| `validation/OrderValidationHandler.java` | Abstract Base Class จัดการ `setNext()` และส่งต่อ `handle()` |
| `validation/OrderValidationContext.java` | Data Context รวบรวม Order, Items, Files, Promotions ที่ใช้ส่งต่อใน Chain |
| `validation/ServiceAvailabilityHandler.java` | ตรวจสอบสถานะการเปิดให้บริการของบริการพิมพ์ |
| `validation/FileTypeValidationHandler.java` | ตรวจสอบความถูกต้องของประเภทไฟล์เอกสาร |
| `validation/QuantityValidationHandler.java` | ตรวจสอบจำนวนชิ้นงานพิมพ์ |
| `validation/PromotionValidityHandler.java` | ตรวจสอบความถูกต้องและช่วงเวลาใช้งานของโปรโมชัน |
| `config/OrderValidationChainConfig.java` | ประกอบ Chain Bean ตามลำดับ: ServiceAvailability → FileType → Quantity → Promotion |
| `service/impl/OrderCommandServiceImpl.java` | เรียกใช้ `orderValidationChain.handle(...)` ก่อนขั้นตอนคำนวณราคาและบันทึก |

### ผลลัพธ์เมื่อ Validation ไม่ผ่าน
- โยน `ValidationException` → ดักจับโดย `GlobalExceptionHandler` (@RestControllerAdvice)
- ตอบกลับไคลเอนต์ด้วย **HTTP 400 Bad Request** พร้อมรายละเอียด Error แบบ JSON

### SOLID ที่เกี่ยวข้อง
- **SRP (Single Responsibility Principle):** แต่ละ Handler รับผิดชอบตรวจสอบกฎเพียงเรื่องเดียวอย่างชัดเจน
- **OCP (Open/Closed Principle):** เพิ่มกฎการตรวจสอบใหม่ได้โดยการสร้าง Handler คลาสใหม่และต่อเข้ากับ Chain ใน Config โดยไม่ต้องแก้ไขโค้ดของ `OrderCommandService`
- **DIP (Dependency Inversion Principle):** `OrderCommandService` พึ่งพา Abstraction (`OrderValidationHandler`) แทนที่จะผูกติดกับ Concrete Handler ตัวใดตัวหนึ่งโดยตรง