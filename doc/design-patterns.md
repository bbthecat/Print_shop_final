# Design Patterns

ทีมเลือกใช้ GoF กลุ่ม **Behavioral** จำนวน 4 pattern (ข้อกำหนดขั้นต่ำ 3)

| Pattern | ปัญหาที่แก้ | ไฟล์/คลาสหลัก | Diagram |
|---|---|---|---|
| Strategy | สูตรคำนวณราคาต่างกันตามประเภทงานพิมพ์ และส่วนลดมี 2 แบบ ถ้ารวมเป็น if-else ต้องแก้โค้ดเดิมทุกครั้งที่เพิ่มประเภท | `service/strategy/pricing/PricingStrategy` + 3 implementation, `PricingStrategyResolver`, `service/strategy/discount/DiscountStrategy` + 2 implementation | [class-diagram.png](diagrams/class-diagram.png) |
| State | Order มี 6 สถานะ แต่ละสถานะอนุญาต action ต่างกัน ถ้าตรวจด้วย if-else กฎจะกระจายไปทุกเมธอดที่แตะสถานะ | `service/state/OrderState`, `AbstractOrderState`, คลาสของทั้ง 6 สถานะ, `OrderStateResolver` | [state-diagram.md](diagrams/state-diagram.md) |
| Observer | สร้าง order / เปลี่ยนสถานะครั้งเดียวต้องทำงานตามหลายอย่าง (สร้าง payment, บันทึกประวัติ, แจ้งเตือน, คืนเงิน) ถ้าเขียนรวมใน service คลาสเดียวจะรับผิดชอบหลายเรื่อง | `service/event/OrderCreatedEvent`, `OrderStatusChangedEvent`, listener 5 ตัวใน `service/listener/` | [status-change-sequence.png](diagrams/status-change-sequence.png) |
| Chain of Responsibility | การสร้าง Order ต้องผ่านการตรวจหลายเงื่อนไขที่ไม่เกี่ยวข้องกัน ถ้ารวมในเมธอดเดียวจะยาวและทดสอบยาก | `validation/OrderValidationHandler` + handler ทั้งหมด, `config/OrderValidationChainConfig` | [create-order-sequence.png](diagrams/create-order-sequence.png) |

**Enterprise / Architectural Patterns ที่ใช้**

| Pattern | ใช้ที่ไหน |
|---|---|
| Layered Architecture | `controller/` → `service/` → `repository/` → `domain/` |
| MVC | Thymeleaf controller ใน `controller/web/` + template ใน `resources/templates/` |
| Repository Pattern | Spring Data JPA ทุกตัวใน `repository/` |
| Service Layer Pattern | business logic และ `@Transactional` อยู่ใน `service/impl/` |
| DTO Pattern | `dto/request/` และ `dto/response/` ไม่ส่ง entity ออก API ตรง ๆ |
| Mapper | `mapper/OrderMapper`, `mapper/PaymentMapper`, `mapper/CustomerMapper` |
| Dependency Injection | Constructor Injection ทุกคลาส ไม่มี `@Autowired` บน field |
## Strategy (P2)

### ปัญหาที่แก้
ราคางานพิมพ์และส่วนลดโปรโมชันมีรูปแบบการคำนวณที่แตกต่างกันตามประเภทบริการ
หากเขียนสูตรทั้งหมดรวมอยู่ใน service เดียวโดยใช้ `switch-case` หรือ `if-else` หลายชั้น จะทำให้:
1. โค้ดมี **High Coupling** และยากต่อการทดสอบแยกส่วน
2. ละเมิด **Open/Closed Principle (OCP)** เพราะทุกครั้งที่มีการเพิ่มประเภทงานพิมพ์ใหม่ (เช่น พิมพ์โปสเตอร์ขนาดใหญ่) หรือโปรโมชันรูปแบบใหม่ จะต้องเปิดโค้ดเดิมมาแก้ไข `if-else` เสมอ ซึ่งเสี่ยงต่อการกระทบกับประเภทเดิมที่ทำงานถูกต้องอยู่แล้ว

### แนวทางแก้ไข
ประยุกต์ใช้ **Strategy Pattern** โดยแยกอัลกอริทึมการคำนวณราคาออกเป็นคลาสเฉพาะกลุ่ม:
1. **PricingStrategy**: กำหนดสัญญากลางสำหรับการคำนวณราคาพิมพ์หลัก (`BLACK_WHITE`, `COLOR`, `PHOTO`)
2. **DiscountStrategy**: กำหนดสัญญากลางสำหรับการคำนวณส่วนลดโปรโมชัน (`PERCENTAGE`, `FIXED_AMOUNT`)
3. **Map-based Resolver**: ใช้ Spring Framework ในการรวบรวม Strategy Beans ทั้งหมด (`List<PricingStrategy>`) และสร้าง `Map<PricingType, PricingStrategy>` ขึ้นมาตอนเริ่มต้นแอปพลิเคชัน (Lookup Table) ทำให้ Resolver สามารถดึง Strategy ที่ต้องการได้ด้วยเวลา O(1) โดยไม่ต้องพึ่งพา `switch-case`

### โครงสร้างไฟล์และคลาสที่ใช้
| ไฟล์ | บทบาท (Role) | หน้าที่ |
|---|---|---|
| `service/strategy/pricing/PricingStrategy.java` | Strategy Interface | กำหนดสัญญา `calculate(basePrice, pageCount, copyCount)` และ `getPricingType()` |
| `service/strategy/pricing/BlackWhitePricingStrategy.java` | Concrete Strategy | คำนวณราคางานพิมพ์ขาวดำมาตรฐาน |
| `service/strategy/pricing/ColorPricingStrategy.java` | Concrete Strategy | คำนวณราคางานพิมพ์สีเลเซอร์ |
| `service/strategy/pricing/PhotoPricingStrategy.java` | Concrete Strategy | คำนวณราคางานพิมพ์ภาพถ่ายคุณภาพสูง |
| `service/strategy/pricing/PricingStrategyResolver.java` | Resolver (Context Helper) | จับคู่ `PricingType` กับ Strategy ผ่าน Map Lookup โดยปราศจาก `if-else` |
| `service/strategy/pricing/PricingCalculator.java` | Context / Facade | เรียก Strategy มาคำนวณราคางานพิมพ์หลัก และรวมค่าบริการเสริม (Addon) ต่อชุด |
| `service/strategy/discount/DiscountStrategy.java` | Strategy Interface | กำหนดสัญญา `calculate(subtotal, promotion)` — ถูกเรียกจาก `OrderCommandServiceImpl.calculateDiscount()` ผ่าน `DiscountStrategyResolver` |
| `service/strategy/discount/PercentageDiscountStrategy.java` | Concrete Strategy | คำนวณส่วนลดแบบคิดเป็นเปอร์เซ็นต์ (%) |
| `service/strategy/discount/FixedAmountDiscountStrategy.java` | Concrete Strategy | คำนวณส่วนลดแบบจำนวนเงินสดคงที่ (บาท) |
| `service/strategy/discount/DiscountStrategyResolver.java` | Resolver (Context Helper) | จับคู่ `DiscountType` กับ Strategy ผ่าน Map Lookup |

### ตารางสูตรคำนวณราคาพิมพ์หลัก (Pricing Strategy)
สูตรการคำนวณของแต่ละ Strategy แตกต่างกันจริงตามลักษณะและโมเดลธุรกิจของงานพิมพ์แต่ละประเภท:
| PricingType | สูตรคำนวณ | เงื่อนไขเฉพาะ / จุดเด่นของ Strategy | ตัวอย่างการคำนวณ |
|---|---|---|---|
| `BLACK_WHITE` | `(basePrice × pageCount) × copyCount` | คิดราคาตามจริงเชิงเส้น เหมาะกับเอกสารและรายงานทั่วไป | ขาวดำ 1.50 บ./หน้า, 20 หน้า, 2 ชุด = (1.50 × 20) × 2 = **60.00 บาท** |
| `COLOR` | `(basePrice × pageCount) × copyCount` *(หาก pageCount > 50 ได้รับส่วนลด 10%)* | มี **Volume Discount** สำหรับงานพิมพ์สีชุดหนา เพื่อสนับสนุนงานพิมพ์เล่มใหญ่ | พิมพ์สี 5.00 บ./หน้า, 60 หน้า, 1 ชุด = (5.00 × 60) = 300 บ. ลด 10% = **270.00 บาท** |
| `PHOTO` | `basePrice × copyCount` | **คิดราคาต่อแผ่นรูปภาพ ไม่คูณจำนวนหน้า** เพราะเป็นงานอัดรูปแผ่นเดี่ยวบนกระดาษโฟโต้คุณภาพสูง | พิมพ์รูป 15.00 บ./แผ่น, 3 แผ่น (copyCount = 3) = 15.00 × 3 = **45.00 บาท** |

### บริการเสริม (Addon Services)
บริการเสริมคิดราคาต่อชุด (Copy) โดยรวมเข้ากับราคางานพิมพ์ใน `PricingCalculator.calculateItemTotal()`:
| บริการเสริม (Addon) | ราคาตัวอย่าง | หน่วยคิดราคา | ตัวอย่างการคำนวณ |
|---|---|---|---|
| เย็บมุม (Corner Staple) | 2.00 บาท | ต่อชุด (copy) | สั่ง 3 ชุด = 2.00 × 3 = **6.00 บาท** |
| เข้าเล่มสันเกลียว (Spiral Binding) | 25.00 บาท | ต่อเล่ม/ชุด (copy) | สั่ง 2 เล่ม = 25.00 × 2 = **50.00 บาท** |
| เข้าเล่มปกแข็ง (Hardcover Binding) | 80.00 บาท | ต่อเล่ม/ชุด (copy) | สั่ง 1 เล่ม = 80.00 × 1 = **80.00 บาท** |
| เคลือบพลาสติก (Lamination) | 10.00 บาท | ต่อชุด (copy) | สั่ง 5 ชุด = 10.00 × 5 = **50.00 บาท** |

**ตัวอย่างเต็ม:** รายงาน 20 หน้า ขาวดำ 1 ชุด + เย็บมุม = (1.50 × 20 × 1) + (2.00 × 1) = **32.00 บาท**
(`pageCount` = จำนวนหน้าต่อชุด, `copyCount` = จำนวนชุด ลูกค้ากรอกแยกกันในหน้าสั่งพิมพ์)

*สูตรรวมใน `PricingCalculator`:*  
$$\text{Item Total} = \text{PrintPrice} + \sum (\text{AddonPrice} \times \text{copyCount})$$
*(ผลลัพธ์คืนค่า $\ge 0$ เสมอ ไม่ติดลบ)*

### ส่วนลดโปรโมชัน (Discount Strategy)
| DiscountType | สูตรคำนวณ | ตัวอย่าง |
|---|---|---|
| `PERCENTAGE` | $\text{orderAmount} \times (\text{value} / 100)$ | ยอด 200 บาท ลด 10% = **20.00 บาท** |
| `FIXED_AMOUNT` | $\min(\text{value}, \text{orderAmount})$ | ยอด 200 บาท ลด 30 บาท = **30.00 บาท** |

### กฎสำคัญทางธุรกิจ (Business Rules)
1. **ผลลัพธ์ไม่ติดลบ:** ทุก Strategy คืนค่า $\ge 0$ เสมอ หากคำนวณได้ค่าลบจะตัดเป็น 0
2. **ไม่เกินยอดรวม:** ส่วนลดต้องไม่เกินยอดสั่งซื้อสุทธิ (ราคาสุทธิหลังลดไม่ติดลบ)
3. **ยอดสั่งซื้อขั้นต่ำ:** โปรโมชันจะใช้ได้เมื่อยอดสั่งซื้อ $\ge$ `minOrderAmount` ถ้าไม่ถึง ระบบแจ้งลูกค้าว่ายอดยังไม่ถึงขั้นต่ำ (ไม่ลด 0 แบบเงียบ ๆ)
4. **ช่วงเวลาที่ใช้งานได้:** ตรวจสอบ `startDate` และ `endDate` เทียบกับเวลาปัจจุบัน พร้อมสถานะ `active = true`
5. **รหัสต้องไม่ซ้ำ:** โค้ดโปรโมชันต้องเป็นตัวพิมพ์ใหญ่และไม่ซ้ำกัน (`UNIQUE`)
6. **ส่วนลดเปอร์เซ็นต์ต้องไม่เกิน 100%:** โปรโมชันประเภท `PERCENTAGE` กำหนดให้ `discountValue` อยู่ระหว่าง 0.01 ถึง 100.00% เท่านั้น (มี Validation ควบคุมทั้งระดับ DTO Request, Controller Form และ Service Layer)

### การวิเคราะห์ตามหลักการ SOLID
* **SRP (Single Responsibility Principle):** แต่ละ Concrete Strategy รับผิดชอบเพียงสูตรคำนวณเฉพาะประเภทของตนเองเท่านั้น ไม่ยุ่งเกี่ยวกับคลังข้อมูลหรือ Web Controller
* **OCP (Open/Closed Principle):** เมื่อต้องการเพิ่มประเภทราคาใหม่ (เช่น `CANVAS_PRINT`) สามารถทำได้โดยสร้างคลาสใหม่ที่ `implements PricingStrategy` และใส่ `@Component` โดย**ไม่ต้องแก้ไขโค้ดเดิม**ใน `PricingStrategyResolver` หรือ `PricingCalculator` เลย
* **LSP (Liskov Substitution Principle):** ทุก Concrete Strategy ปฏิบัติตามสัญญาของ Interface อย่างเคร่งครัด คืนค่า `BigDecimal >= 0` เสมอ และไม่มีการ throw `UnsupportedOperationException` ทำให้สามารถสลับการใช้งานระหว่าง Strategy ใดๆ ได้อย่างปลอดภัย
* **ISP (Interface Segregation Principle):** อินเทอร์เฟซ `PricingStrategy` และ `DiscountStrategy` มีขนาดกะทัดรัด มีเฉพาะเมธอดที่เกี่ยวข้องกับการคำนวณเท่านั้น ไม่บังคับให้คลาสลูกต้อง implement เมธอดที่ไม่ได้ใช้
* **DIP (Dependency Inversion Principle):** คลาสระดับสูงอย่าง `PricingCalculator` และ `OrderCommandServiceImpl` ขึ้นอยู่กับ Abstraction (`PricingStrategyResolver`, `PricingStrategy`, `DiscountStrategy`) แทนที่จะผูกติดกับ Concrete Class โดยตรง

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
ทุกครั้งที่สร้าง order หรือสถานะเปลี่ยน มีงานตามหลังหลายอย่าง: สร้าง payment, บันทึกประวัติ, แจ้งเตือนลูกค้า/พนักงาน, คืนเงินเมื่อยกเลิก
ถ้าเขียนรวมใน `OrderStatusService` คลาสเดียวจะรับผิดชอบหลายเรื่อง
และทุกครั้งที่เพิ่มงานตามหลังใหม่ต้องกลับมาแก้ service เดิม

### แนวทาง
ใช้ `ApplicationEventPublisher` ของ Spring

OrderCommandService สร้าง order เสร็จ → publish `OrderCreatedEvent`
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
| Listener | ฟัง event | ทำอะไร | เขียนลงตาราง |
|---|---|---|---|
| `PaymentCreationListener` | `OrderCreatedEvent` | สร้าง payment แบบ UNPAID จากยอดของ order (ถ้ายังไม่มี) | `payments` |
| `StaffNewOrderListener` | `OrderCreatedEvent` | แจ้งเตือนพนักงาน (STAFF) ทุกคนว่ามีงานใหม่ | `notifications` |
| `OrderHistoryListener` | `OrderStatusChangedEvent` | บันทึกว่าใครเปลี่ยนจากสถานะไหนเป็นสถานะไหน เมื่อไหร่ | `order_status_histories` |
| `InAppNotificationListener` | `OrderStatusChangedEvent` | สร้างการแจ้งเตือนในระบบให้เจ้าของคำสั่งซื้อ | `notifications` |
| `PaymentRefundListener` | `OrderStatusChangedEvent` | order ถูกยกเลิก → payment ที่จ่ายแล้วเปลี่ยนเป็น REFUNDED (รายงานยอดขายจะไม่นับ) | `payments` |

ทั้ง flow ทดสอบจริงใน `ObserverIntegrationTest` (`@SpringBootTest` + ฐานข้อมูล H2)

### ผลลัพธ์
เพิ่มช่องทางแจ้งเตือนในอนาคต (Email / LINE) = เพิ่ม Listener ใหม่หนึ่งคลาส
ไม่ต้องแตะ `OrderStatusService` เลย — `PaymentRefundListener` และ `StaffNewOrderListener` ก็เพิ่มเข้ามาทีหลังด้วยวิธีนี้

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
            (โยน 400 ถ้าไม่พบ/ปิด)       (โยน 400 ถ้าไฟล์ผิด)         (โยน 400 ถ้าหน้า/ชุด<=0)   (โยน 400 ถ้าโปรโมชันหมดอายุ)
```

| ลำดับ | Handler | สิ่งที่ตรวจสอบ | ข้อยกเว้น/ข้อความ Error |
|---|---|---|---|
| 1 | `ServiceAvailabilityHandler` | ตรวจสอบว่า `serviceId` ของทุก Item มีอยู่จริงและ `active = true` ผ่าน `ServiceCatalogQueryService` | `ValidationException` / `ResourceNotFoundException` ("Service not found with ID: {id}") |
| 2 | `FileTypeValidationHandler` | ตรวจสอบ Mime Type ของไฟล์ที่แนบ รองรับเฉพาะ `application/pdf`, `image/jpeg`, `image/png` | `ValidationException` ("รองรับเฉพาะไฟล์ PDF, JPG และ PNG ...") — ชนิดไฟล์เดาจากนามสกุลของชื่อไฟล์ที่ลูกค้ากรอก |
| 3 | `QuantityValidationHandler` | ตรวจสอบว่าจำนวนชุด (`quantity`) และจำนวนหน้า (`pageCount`) ของแต่ละ Item ต้องมากกว่า 0 | `ValidationException` ("Quantity must be greater than 0" / "Page count must be greater than 0") |
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
