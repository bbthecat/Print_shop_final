# SOLID Analysis — PrintFlow

ตารางนี้ระบุว่าหลักการ SOLID แต่ละข้อปรากฏที่ไหนในโค้ดจริง พร้อมเหตุผล
เลขบรรทัดอ้างอิงโค้ดใน `code/src/main/java/com/printflow/` ณ code freeze (branch `develop`)

---

## S — Single Responsibility Principle

| ไฟล์/คลาส | บรรทัด | หน้าที่เดียวที่รับผิดชอบ | ผู้รับผิดชอบ |
|---|---|---|---|
| `repository/ReportRepository.java` | L18 | รวม query อ่านอย่างเดียวของรายงานไว้ที่เดียว ไม่ปนกับ repository ของ Order/Payment ที่ใช้เขียนข้อมูล | P1 |
| `exception/GlobalExceptionHandler.java` | L24 | แปลง exception ของทุก REST API เป็น JSON error รูปแบบเดียว controller ไม่ต้อง try/catch เอง | P1 |
| `mapper/CustomerMapper.java` | L12 | แปลง Entity ↔ DTO อย่างเดียว service ไม่ต้องรู้รูปแบบ response | P1 |
| `service/impl/ServiceCatalogServiceImpl.java` | L21 | จัดการข้อมูลบริการ/บริการเสริมอย่างเดียว การคำนวณราคาแยกไปอยู่ใน `PricingCalculator` | P2 |
| `service/strategy/pricing/PricingCalculator.java` | L15 | คำนวณราคาของ 1 รายการพิมพ์ (ราคาพิมพ์ + บริการเสริม) อย่างเดียว | P2 |
| `validation/QuantityValidationHandler.java` | L6 | ตรวจจำนวนหน้า/จำนวนชุดอย่างเดียว ข้อตรวจอื่นอยู่ใน handler ของตัวเอง | P3 |
| `mapper/OrderMapper.java` | L14 | ประกอบ `OrderResponse` จาก entity อย่างเดียว ไม่ query ฐานข้อมูลเอง | P3 |
| `service/listener/OrderHistoryListener.java` | L10 | บันทึกประวัติการเปลี่ยนสถานะอย่างเดียว ไม่ยุ่งกับการแจ้งเตือน | P4 |
| `service/listener/InAppNotificationListener.java` | L12 | สร้างการแจ้งเตือนให้ลูกค้าอย่างเดียว ไม่ยุ่งกับประวัติ | P4 |
| `service/listener/PaymentRefundListener.java` | L14 | เปลี่ยน payment เป็น REFUNDED เมื่อ order ถูกยกเลิกอย่างเดียว | P4 |

---

## O — Open/Closed Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `config/SecurityConfig.java` | L31 | กฎสิทธิ์อยู่ที่เดียว เพิ่ม endpoint ใหม่ = เพิ่ม `requestMatchers` หนึ่งบรรทัด ไม่ต้องแก้ controller | P1 |
| `service/strategy/pricing/PricingStrategyResolver.java` | L18 | เพิ่มประเภทงานพิมพ์ใหม่ = เพิ่มคลาส `PricingStrategy` ใหม่ (Spring ฉีดเข้า List ให้เอง) ไม่ต้องแก้ resolver หรือ calculator | P2 |
| `service/strategy/discount/DiscountStrategyResolver.java` | L18 | เพิ่มประเภทส่วนลดใหม่ = เพิ่มคลาส `DiscountStrategy` ใหม่ `OrderCommandServiceImpl` ไม่ต้องแก้ | P2 |
| `config/OrderValidationChainConfig.java` | L16 | เพิ่มขั้นตรวจใหม่ = สร้าง handler ใหม่แล้วต่อเข้า chain ในไฟล์นี้ ไม่ต้องแก้ service | P3 |
| `service/state/OrderStateResolver.java` | L28 | เพิ่มสถานะใหม่ = เพิ่มคลาส state แล้วลงทะเบียนใน List ไม่ต้องแก้คลาสสถานะเดิมหรือ service | P4 |
| `service/event/OrderStatusChangedEvent.java` | L5 | เพิ่มสิ่งที่ต้องทำเมื่อสถานะเปลี่ยน (เช่น คืนเงิน, Email) = เพิ่ม listener ใหม่ ไม่ต้องแตะ `OrderStatusServiceImpl` — `PaymentRefundListener` เพิ่มเข้ามาทีหลังด้วยวิธีนี้ | P4 |

---

## L — Liskov Substitution Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `security/SecurityContextCurrentUserProvider.java` | L9 | ใช้แทน `CurrentUserProvider` ได้ทุกที่ ในเทสต์ใช้ mock แทนได้โดย controller ทำงานเหมือนเดิม | P1 |
| `service/strategy/pricing/PhotoPricingStrategy.java` | L17 | สูตรต่างจากตัวอื่น (คิดต่อแผ่น) แต่รับ/คืนค่าตามสัญญาเดียวกับ `PricingStrategy` ใช้แทนกันได้ใน `PricingCalculator` | P2 |
| `validation/FileTypeValidationHandler.java` | L8 | handler ทุกตัวสืบทอด `OrderValidationHandler` และทำงานตามสัญญา `validate()` เดียวกัน สลับลำดับหรือแทนกันใน chain ได้ | P3 |
| `service/state/AbstractOrderState.java` | L45 | ทุก state คืนค่า `OrderStatus` ตามสัญญาเดียวกัน การปฏิเสธใช้ `InvalidStateTransitionException` ซึ่งเป็น exception ของ domain ไม่มีตัวใดโยน `UnsupportedOperationException` | P4 |
| `service/state/CompletedState.java` | L9 | สถานะสุดท้ายยังสลับใช้แทน state อื่นได้โดยไม่ทำให้ caller พัง เพราะใช้พฤติกรรมตั้งต้นจากคลาสแม่ | P4 |

---

## I — Interface Segregation Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `service/CustomerService.java` / `service/AdminUserService.java` | L9 / L9 | แยกงานของลูกค้า (สมัคร, แก้โปรไฟล์) ออกจากงานของ Admin (สร้าง STAFF, เปลี่ยน role) controller แต่ละฝั่งพึ่งเฉพาะที่ใช้ | P1 |
| `security/CurrentUserProvider.java` | L3 | interface เล็กมี 2 เมธอด (id ของคนที่ login, เป็น STAFF/ADMIN ไหม) ใช้ได้ทุก controller โดยไม่ต้องพึ่ง Spring Security โดยตรง | P1 |
| `service/ServiceCatalogQueryService.java` / `service/ServiceCatalogCommandService.java` | L10 / L9 | แยกการอ่าน (หน้าลูกค้า) ออกจากการเขียน (หน้า Admin) ฝั่งที่อ่านอย่างเดียวไม่ต้องเห็นเมธอดแก้ไข | P2 |
| `service/OrderCommandService.java` / `service/OrderQueryService.java` | L6 / L11 | แยกสร้าง/ลบ order ออกจากการค้นหา order | P3 |
| `service/OrderStatusService.java` | L8 | แยกเรื่องสถานะออกจาก `OrderCommandService` / `OrderQueryService` ฝั่งที่สนใจแค่เปลี่ยนสถานะไม่ต้องพึ่ง interface CRUD ทั้งก้อน | P4 |
| `service/NotificationService.java` | L7 | มีเฉพาะเมธอดเกี่ยวกับการแจ้งเตือน ไม่ผูกกับ Order | P4 |

---

## D — Dependency Inversion Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `service/impl/CustomerServiceImpl.java` | L27 | รับ `UserRepository`, `PasswordEncoder`, `CustomerMapper` ผ่าน constructor และขึ้นกับ interface ของ Spring (`PasswordEncoder`) ไม่ใช่ `BCryptPasswordEncoder` ตรง ๆ | P1 |
| `service/impl/AdminUserServiceImpl.java` | L29 | ขึ้นกับ `CurrentUserProvider` (interface) เพื่อเช็กว่าไม่ได้แก้บัญชีตัวเอง ไม่อ่าน `SecurityContextHolder` เอง | P1 |
| `service/strategy/pricing/PricingCalculator.java` | L15 | ขึ้นกับ `PricingStrategyResolver` และ interface `PricingStrategy` ไม่รู้จักคลาสสูตรตัวจริง | P2 |
| `service/impl/OrderCommandServiceImpl.java` | L61 | รับทุก dependency ผ่าน constructor และใช้ interface (`OrderValidationHandler`, `DiscountStrategyResolver`, `OrderQueryService`, `ApplicationEventPublisher`) ทำให้เทสต์ใส่สูตรราคา/ส่วนลดตัวจริงหรือ mock ได้ | P3 |
| `service/impl/OrderStatusServiceImpl.java` | L34 | รับ dependency ผ่าน constructor ทั้งหมด และขึ้นกับ abstraction (`OrderRepository`, `ApplicationEventPublisher`) ไม่ใช่คลาส concrete | P4 |
| `service/listener/InAppNotificationListener.java` | L12 | ขึ้นกับ interface `NotificationService` ไม่ใช่ `NotificationServiceImpl` ทำให้ test ใช้ mock ได้ | P4 |
