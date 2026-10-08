# Data Dictionary — PrintFlow Database (13 Tables)

เอกสารพจนานุกรมข้อมูล (Data Dictionary) อธิบายโครงสร้างฐานข้อมูลเชิงสัมพันธ์ทั้งหมดของระบบ PrintFlow ครอบคลุมทั้ง 13 ตารางตาม Flyway Migrations (`V1` ถึง `V4`)

---

## สารบัญตาราง (Table Index)
| ลำดับ | ชื่อตาราง | คำอธิบาย | หมวดหมู่ / ผู้รับผิดชอบ |
|:---:|---|---|:---:|
| 1 | `users` | ข้อมูลบัญชีผู้ใช้งานระบบและสิทธิ์การเข้าถึง | User & Auth (P1) |
| 2 | `user_profiles` | ข้อมูลโปรไฟล์ส่วนตัวของผู้ใช้ (ชื่อ, เบอร์โทร, ที่อยู่) | User & Auth (P1) |
| 3 | `print_services` | รายการบริการงานพิมพ์หลักและราคาเริ่มต้น | Catalog & Pricing (P2) |
| 4 | `addon_services` | รายการบริการเสริม (เข้าเล่ม, เคลือบ, เย็บมุม) | Catalog & Pricing (P2) |
| 5 | `promotions` | ข้อมูลโค้ดส่วนลดและโปรโมชัน | Catalog & Pricing (P2) |
| 6 | `print_orders` | ข้อมูลคำสั่งพิมพ์ สถานะ และยอดรวม | Order & Workflow (P3) |
| 7 | `print_items` | รายการบริการที่สั่งพิมพ์ในแต่ละคำสั่งซื้อ | Order & Workflow (P3) |
| 8 | `print_item_addons` | รายการบริการเสริมที่ผูกกับแต่ละรายการพิมพ์ | Order & Workflow (P3) |
| 9 | `order_promotions` | โปรโมชันที่ถูกใช้งานในแต่ละคำสั่งซื้อ | Order & Workflow (P3) |
| 10 | `order_files` | ข้อมูลไฟล์งานพิมพ์ที่ลูกค้าอัปโหลด | Order & Workflow (P3) |
| 11 | `payments` | ข้อมูลการชำระเงินและสถานะยอดเงิน | Payment & Tracking (P4) |
| 12 | `order_status_histories` | ประวัติการเปลี่ยนสถานะของคำสั่งซื้อ | Payment & Tracking (P4) |
| 13 | `notifications` | ข้อมูลการแจ้งเตือนสำหรับผู้ใช้งาน | Payment & Tracking (P4) |

---

## 1. ตาราง `users`
เก็บข้อมูลบัญชีผู้ใช้งานสำหรับตรวจสอบสิทธิ์ (Authentication & Authorization)
* **PrimaryKey:** `id`
* **Unique Keys:** `username`, `email`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสผู้ใช้งาน (PK) |
| `username` | VARCHAR(50) | No | — | ชื่อบัญชีผู้ใช้ (Unique) |
| `email` | VARCHAR(100) | No | — | อีเมลสำหรับติดต่อและเข้าสู่ระบบ (Unique) |
| `password_hash` | VARCHAR(255) | No | — | รหัสผ่านที่เข้ารหัสด้วย BCrypt |
| `role` | VARCHAR(20) | No | — | บทบาทผู้ใช้ (`CUSTOMER`, `STAFF`, `ADMIN`) |
| `active` | BOOLEAN | No | `TRUE` | สถานะเปิดใช้งานบัญชี (Soft Delete) |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่สร้างบัญชี |
| `updated_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่แก้ไขข้อมูลล่าสุด |

---

## 2. ตาราง `user_profiles`
เก็บข้อมูลส่วนบุคคลของผู้ใช้งาน (ความสัมพันธ์ 1:1 กับ `users`)
* **PrimaryKey:** `id`
* **Foreign Key:** `user_id` อ้างอิงไปยัง `users(id)` (ON DELETE CASCADE, Unique)

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสโปรไฟล์ (PK) |
| `user_id` | BIGINT | No | — | รหัสผู้ใช้เจ้าของโปรไฟล์ (FK, Unique) |
| `first_name` | VARCHAR(50) | No | — | ชื่อจริง |
| `last_name` | VARCHAR(50) | No | — | นามสกุล |
| `phone` | VARCHAR(20) | Yes | — | หมายเลขโทรศัพท์ |
| `address` | TEXT | Yes | — | ที่อยู่สำหรับติดต่อหรือจัดส่ง |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่สร้างโปรไฟล์ |
| `updated_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่แก้ไขข้อมูลล่าสุด |

---

## 3. ตาราง `print_services`
เก็บข้อมูลบริการงานพิมพ์หลักที่ทางร้านเปิดให้บริการ
* **PrimaryKey:** `id`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสบริการงานพิมพ์ (PK) |
| `name` | VARCHAR(100) | No | — | ชื่อบริการ (เช่น เอกสารขาวดำ A4, โฟโต้สี) |
| `description` | TEXT | Yes | — | คำอธิบายรายละเอียดของบริการ |
| `base_price` | NUMERIC(10,2) | No | — | ราคาเริ่มต้น (บาทต่อหน้า) |
| `pricing_type` | VARCHAR(20) | No | — | ประเภทการคำนวณราคา (`BLACK_WHITE`, `COLOR`, `PHOTO`) |
| `active` | BOOLEAN | No | `TRUE` | สถานะเปิดใช้งานบริการ |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่เพิ่มบริการ |
| `updated_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่แก้ไขล่าสุด |

---

## 4. ตาราง `addon_services`
เก็บข้อมูลบริการเสริม เช่น เข้าเล่มสันเกลียว, เคลือบพลาสติก, เย็บมุม
* **PrimaryKey:** `id`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสบริการเสริม (PK) |
| `name` | VARCHAR(100) | No | — | ชื่อบริการเสริม (เช่น เข้าเล่มสันเกลียว) |
| `description` | TEXT | Yes | — | รายละเอียดบริการเสริม |
| `price` | NUMERIC(10,2) | No | — | ราคาค่าบริการเสริม (บาทต่อชุด/เล่ม) |
| `active` | BOOLEAN | No | `TRUE` | สถานะเปิดใช้งานบริการเสริม |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่เพิ่มบริการ |
| `updated_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่แก้ไขล่าสุด |

---

## 5. ตาราง `promotions`
เก็บรหัสโปรโมชันและส่วนลด
* **PrimaryKey:** `id`
* **Unique Key:** `code`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสโปรโมชัน (PK) |
| `code` | VARCHAR(50) | No | — | โค้ดส่วนลด (Unique, ตัวพิมพ์ใหญ่) |
| `description` | TEXT | Yes | — | รายละเอียดและเงื่อนไขโปรโมชัน |
| `discount_type` | VARCHAR(20) | No | — | ประเภทส่วนลด (`PERCENTAGE`, `FIXED_AMOUNT`) |
| `discount_value` | NUMERIC(10,2) | No | — | มูลค่าส่วนลด (% หรือ บาท) |
| `min_order_amount` | NUMERIC(10,2) | No | `0.00` | ยอดสั่งซื้อขั้นต่ำที่สามารถใช้โค้ดได้ |
| `start_date` | TIMESTAMP | No | — | วันและเวลาเริ่มต้นใช้งาน |
| `end_date` | TIMESTAMP | No | — | วันและเวลาสิ้นสุดใช้งาน |
| `active` | BOOLEAN | No | `TRUE` | สถานะเปิดใช้งานโปรโมชัน |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่สร้างโปรโมชัน |
| `updated_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่แก้ไขล่าสุด |

---

## 6. ตาราง `print_orders`
เก็บข้อมูลคำสั่งพิมพ์หลักของลูกค้า
* **PrimaryKey:** `id`
* **Unique Key:** `order_number`
* **Foreign Key:** `user_id` อ้างอิงไปยัง `users(id)`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสคำสั่งพิมพ์ (PK) |
| `order_number` | VARCHAR(50) | No | — | หมายเลขคำสั่งซื้อ (เช่น `ORD-172845...`, Unique) |
| `user_id` | BIGINT | No | — | รหัสลูกค้าผู้สั่งพิมพ์ (FK) |
| `status` | VARCHAR(30) | No | — | สถานะคำสั่งซื้อ (`PENDING`, `CONFIRMED`, `PROCESSING`, `READY`, `COMPLETED`, `CANCELLED`) |
| `total_price` | NUMERIC(12,2) | No | — | ราคารวมสุทธิหลังหักส่วนลด (บาท) |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่สร้างคำสั่งพิมพ์ |

---

## 7. ตาราง `print_items`
เก็บรายการงานพิมพ์แต่ละชิ้นในคำสั่งซื้อ (ความสัมพันธ์ 1:N กับ `print_orders`)
* **PrimaryKey:** `id`
* **Foreign Keys:**
  * `order_id` อ้างอิงไปยัง `print_orders(id)` (ON DELETE CASCADE)
  * `service_id` อ้างอิงไปยัง `print_services(id)`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสรายการงานพิมพ์ (PK) |
| `order_id` | BIGINT | No | — | รหัสคำสั่งพิมพ์หลัก (FK) |
| `service_id` | BIGINT | No | — | รหัสบริการงานพิมพ์ที่เลือก (FK) |
| `quantity` | INTEGER | No | — | จำนวนชุดที่สั่งพิมพ์ (Copy Count) |
| `unit_price` | NUMERIC(12,2) | No | — | ราคาเฉลี่ยต่อชุด (บาท) |
| `subtotal` | NUMERIC(12,2) | No | — | ยอดรวมของรายการนี้ (บาท) |

---

## 8. ตาราง `print_item_addons`
เก็บบริการเสริมที่ถูกเลือกในแต่ละรายการพิมพ์
* **PrimaryKey:** `id`
* **Foreign Keys:**
  * `item_id` อ้างอิงไปยัง `print_items(id)` (ON DELETE CASCADE)
  * `addon_id` อ้างอิงไปยัง `addon_services(id)`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสรายการบริการเสริมที่เลือก (PK) |
| `item_id` | BIGINT | No | — | รหัสรายการงานพิมพ์ที่ผูกไว้ (FK) |
| `addon_id` | BIGINT | No | — | รหัสบริการเสริมที่เลือก (FK) |
| `price` | NUMERIC(12,2) | No | — | ราคาบริการเสริม ณ เวลาที่สั่งซื้อ (บาท) |

---

## 9. ตาราง `order_promotions`
เก็บบันทึกการใช้โปรโมชันในคำสั่งซื้อ
* **PrimaryKey:** `id`
* **Foreign Keys:**
  * `order_id` อ้างอิงไปยัง `print_orders(id)` (ON DELETE CASCADE)
  * `promotion_id` อ้างอิงไปยัง `promotions(id)`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสการใช้โปรโมชัน (PK) |
| `order_id` | BIGINT | No | — | รหัสคำสั่งพิมพ์ (FK) |
| `promotion_id` | BIGINT | No | — | รหัสโปรโมชันที่นำมาใช้ (FK) |
| `discount_amount` | NUMERIC(12,2) | No | — | จำนวนเงินส่วนลดที่หักออกจากยอดคำสั่งซื้อ (บาท) |

---

## 10. ตาราง `order_files`
เก็บข้อมูลไฟล์เอกสารที่ลูกค้าอัปโหลดประกอบคำสั่งพิมพ์
* **PrimaryKey:** `id`
* **Foreign Key:** `order_id` อ้างอิงไปยัง `print_orders(id)` (ON DELETE CASCADE)

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสไฟล์งานพิมพ์ (PK) |
| `order_id` | BIGINT | No | — | รหัสคำสั่งพิมพ์ (FK) |
| `file_name` | VARCHAR(255) | No | — | ชื่อไฟล์ดั้งเดิมที่อัปโหลด |
| `file_path` | VARCHAR(500) | No | — | เส้นทางที่เก็บไฟล์บนเซิร์ฟเวอร์/Storage |
| `file_type` | VARCHAR(100) | No | — | ชนิดของไฟล์ (MIME Type เช่น `application/pdf`) |
| `file_size` | BIGINT | Yes | — | ขนาดของไฟล์ (Bytes) |

---

## 11. ตาราง `payments`
เก็บข้อมูลและสถานะการชำระเงินของคำสั่งซื้อ
* **PrimaryKey:** `id`
* **Foreign Key:** `order_id` อ้างอิงไปยัง `print_orders(id)` (ON DELETE CASCADE, Unique)

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสการชำระเงิน (PK) |
| `order_id` | BIGINT | No | — | รหัสคำสั่งพิมพ์ (FK, Unique) |
| `amount` | NUMERIC(12,2) | No | — | ยอดเงินที่ต้องชำระ (บาท) |
| `payment_method` | VARCHAR(30) | Yes | — | วิธีการชำระเงิน (เช่น `PROMPT_PAY`, `BANK_TRANSFER`) |
| `payment_status` | VARCHAR(30) | No | — | สถานะการชำระเงิน (`UNPAID`, `PAID`, `REJECTED`) |
| `paid_at` | TIMESTAMP | Yes | — | วันและเวลาที่ลูกค้าโอนเงินสำเร็จ |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่สร้างรายการชำระเงิน |

---

## 12. ตาราง `order_status_histories`
บันทึก Audit Trail ของการเปลี่ยนสถานะคำสั่งซื้อ
* **PrimaryKey:** `id`
* **Foreign Keys:**
  * `order_id` อ้างอิงไปยัง `print_orders(id)`
  * `changed_by` อ้างอิงไปยัง `users(id)`

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสประวัติสถานะ (PK) |
| `order_id` | BIGINT | No | — | รหัสคำสั่งพิมพ์ (FK) |
| `old_status` | VARCHAR(30) | Yes | — | สถานะเดิมก่อนเปลี่ยน |
| `new_status` | VARCHAR(30) | No | — | สถานะใหม่ที่เปลี่ยนไป |
| `changed_by` | BIGINT | No | — | รหัสผู้ใช้งานที่กดเปลี่ยนสถานะ (FK) |
| `changed_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่มีการเปลี่ยนสถานะ |

---

## 13. ตาราง `notifications`
เก็บประวัติการแจ้งเตือนในระบบ (In-App Notifications)
* **PrimaryKey:** `id`
* **Foreign Keys:**
  * `user_id` อ้างอิงไปยัง `users(id)`
  * `order_id` อ้างอิงไปยัง `print_orders(id)` (ON DELETE CASCADE, Nullable)

| ชื่อฟิลด์ | ชนิดข้อมูล | ค่าว่าง (Null) | ค่าเริ่มต้น (Default) | คำอธิบาย |
|---|---|:---:|---|---|
| `id` | BIGSERIAL | No | Auto increment | รหัสการแจ้งเตือน (PK) |
| `user_id` | BIGINT | No | — | รหัสผู้ใช้ที่ได้รับการแจ้งเตือน (FK) |
| `order_id` | BIGINT | Yes | — | รหัสคำสั่งพิมพ์ที่เกี่ยวข้อง (FK, Nullable) |
| `title` | VARCHAR(100) | No | — | หัวข้อการแจ้งเตือน |
| `message` | VARCHAR(500) | No | — | เนื้อหาข้อความแจ้งเตือน |
| `is_read` | BOOLEAN | No | `FALSE` | สถานะการอ่านแจ้งเตือน (True = อ่านแล้ว) |
| `created_at` | TIMESTAMP | No | `CURRENT_TIMESTAMP` | วันและเวลาที่ส่งการแจ้งเตือน |
