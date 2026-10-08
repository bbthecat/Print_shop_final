# SOLID Analysis — PrintFlow

ตารางนี้ระบุว่าหลักการแต่ละข้อปรากฏที่ไหนในโค้ดจริง พร้อมเหตุผล
เลขบรรทัดเติมหลังประกาศ Code Freeze เพราะก่อนหน้านั้นโค้ดยังขยับ

---

## S — Single Responsibility Principle

| ไฟล์/คลาส | บรรทัด | หน้าที่เดียวที่รับผิดชอบ | ผู้รับผิดชอบ |
|---|---|---|---|
| `service/listener/OrderHistoryListener.java` | L_ | บันทึกประวัติการเปลี่ยนสถานะอย่างเดียว ไม่ยุ่งกับการแจ้งเตือน | P4 |
| `service/listener/InAppNotificationListener.java` | L_ | สร้างการแจ้งเตือนอย่างเดียว ไม่ยุ่งกับประวัติ | P4 |
| `service/impl/OrderStatusServiceImpl.java` | L_ | ทำหน้าที่ orchestration ของการเปลี่ยนสถานะ ส่วนกฎอยู่ในคลาส state | P4 |

---

## O — Open/Closed Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `service/state/OrderStateResolver.java` | L_ | เพิ่มสถานะใหม่ = เพิ่มคลาส state แล้วลงทะเบียนใน List ไม่ต้องแก้คลาสสถานะเดิมหรือ service | P4 |
| `service/event/OrderStatusChangedEvent.java` | L_ | เพิ่มช่องทางแจ้งเตือนใหม่ (Email/LINE) = เพิ่ม listener ใหม่ ไม่ต้องแตะ `OrderStatusServiceImpl` | P4 |

---

## L — Liskov Substitution Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `service/state/AbstractOrderState.java` | L_ | ทุก state คืนค่า `OrderStatus` ตามสัญญาเดียวกัน การปฏิเสธใช้ `InvalidStateTransitionException` ซึ่งเป็น exception ของ domain ที่ประกาศไว้ ไม่มีตัวใดโยน `UnsupportedOperationException` | P4 |
| `service/state/CompletedState.java` | L_ | สถานะสุดท้ายยังสลับใช้แทน state อื่นได้โดยไม่ทำให้ caller พัง เพราะใช้พฤติกรรมตั้งต้นจากคลาสแม่ | P4 |

---

## I — Interface Segregation Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `service/OrderStatusService.java` | L_ | แยกเรื่องสถานะออกจาก `OrderCommandService` / `OrderQueryService` ฝั่งที่สนใจแค่เปลี่ยนสถานะไม่ต้องพึ่ง interface ที่มีเมธอด CRUD ทั้งก้อน | P4 |
| `service/NotificationService.java` | L_ | มีเฉพาะเมธอดเกี่ยวกับการแจ้งเตือน ไม่ผูกกับ Order | P4 |

---

## D — Dependency Inversion Principle

| ไฟล์/คลาส | บรรทัด | เหตุผล | ผู้รับผิดชอบ |
|---|---|---|---|
| `service/impl/OrderStatusServiceImpl.java` | L_ | รับ dependency ผ่าน constructor ทั้งหมด และขึ้นกับ abstraction (`OrderRepository`, `ApplicationEventPublisher`) ไม่ใช่คลาส concrete | P4 |
| `service/listener/InAppNotificationListener.java` | L_ | ขึ้นกับ interface `NotificationService` ไม่ใช่ `NotificationServiceImpl` ทำให้ test ใช้ mock ได้ | P4 |