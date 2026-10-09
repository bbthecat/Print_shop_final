# Test Report — PrintFlow

| รายการ | ผล |
|---|---|
| วันที่รัน | 9 ต.ค. 2026 (code freeze, branch `develop`) |
| คำสั่ง | `cd code && mvn clean test && mvn surefire-report:report-only` |
| จำนวน test | **241** |
| ผ่าน / ล้มเหลว / Error / ข้าม | **241 / 0 / 0 / 0** |
| รายงาน HTML ฉบับเต็ม | [`html/surefire.html`](html/surefire.html) (เปิดในเบราว์เซอร์) |

CI (GitHub Actions) รันชุดเดียวกันนี้ทุกครั้งที่ push / เปิด PR และแนบรายงานเป็น artifact

## ประเภทของ test

| ประเภท | เครื่องมือ | ทดสอบอะไร |
|---|---|---|
| Unit test (Service, Strategy, State, Listener, Validation) | JUnit 5 + Mockito | กฎธุรกิจ สูตรราคา/ส่วนลด การเปลี่ยนสถานะ และ chain ตรวจคำสั่งพิมพ์ |
| Controller test | `@WebMvcTest` + Spring Security Test | HTTP status, JSON, หน้าเว็บ, สิทธิ์ตาม role, ห้ามดูข้อมูลของคนอื่น, CSRF |
| Repository test | `@DataJpaTest` + H2 | query ของ Order และรายงาน (JPQL จริงบนฐานข้อมูล) |
| Integration test | `@SpringBootTest` + H2 | flow จริงทั้งระบบผ่าน Observer: สร้าง order → ยืนยัน → ชำระเงิน → ยกเลิก (คืนเงิน) |

## ผลรายคลาส

| เจ้าของ | Test class | จำนวน |
|---|---|---|
| P1 | `CustomerControllerTest` | 16 |
| P1 | `CustomerServiceImplTest` | 8 |
| P1 | `AdminUserControllerTest` / `AdminUserServiceImplTest` / `AdminUserPageControllerTest` | 7 / 9 / 8 |
| P1 | `AuthPageControllerTest` | 9 |
| P1 | `ProfileControllerTest` | 5 |
| P1 | `ReportControllerTest` / `ReportServiceImplTest` / `ReportRepositoryTest` / `AdminReportPageControllerTest` | 4 / 6 / 5 / 4 |
| P1 | `CatalogSecurityTest` / `OrderSecurityTest` | 10 / 11 |
| P1 | `ErrorPageTest` | 2 |
| P2 | `PricingStrategyTest` (BlackWhite, Color, Photo, Resolver, Calculator) | 14 |
| P2 | `DiscountStrategyTest` (Percentage, FixedAmount, Resolver) | 12 |
| P2 | `PrintServiceControllerTest` / `PromotionControllerTest` / `ServiceWebControllerTest` | 7 / 6 / 1 |
| P2 | `AdminServicePageControllerTest` / `AdminPromotionPageControllerTest` / `AdminCatalogEditPageTest` | 4 / 6 / 8 |
| P3 | `OrderCommandServiceTest` (สูตรราคาจริง เช่น 20 หน้า + เย็บมุม = 32 บาท, ตัด addon ซ้ำ) | 11 |
| P3 | `OrderQueryServiceImplTest` / `OrderRepositoryTest` | 3 / 5 |
| P3 | `OrderControllerTest` / `OrderWebControllerTest` | 4 / 12 |
| P3 | `OrderValidationChainTest` | 8 |
| P4 | `OrderStateTest` | 10 |
| P4 | `OrderStatusServiceTest` (รวมลูกค้ายกเลิกเอง, สถานะถัดไปที่เลือกได้) | 8 |
| P4 | `PaymentServiceImplTest` (กันจ่ายซ้ำ, ห้ามจ่าย order ที่ยกเลิก) | 4 |
| P4 | `OrderStatusListenerTest` / `PaymentCreationListenerTest` / `NewObserverListenersTest` | 4 / 1 / 3 |
| P4 | `StaffPagesControllerTest` | 5 |
| ทีม | `ObserverIntegrationTest` | 1 |
| | **รวม** | **241** |

## Test case สำคัญที่ใช้ตอบคำถามตอนนำเสนอ

| เรื่อง | Test | ผลที่คาดหวัง |
|---|---|---|
| สูตรราคา + บริการเสริมคิดต่อชุด | `OrderCommandServiceTest.createOrder_20PagesOneSetWithStaple_chargesStapleOnce` | 1.50 × 20 + 2.00 × 1 = 32.00 บาท |
| ใช้ Strategy ส่วนลดจริง | `OrderCommandServiceTest.createOrder_withPercentagePromotion_usesDiscountStrategy` | ยอด 150 ลด 10% = 15.00 บาท |
| ยอดไม่ถึงขั้นต่ำโปรโมชัน | `OrderCommandServiceTest.createOrder_belowPromotionMinimum_throwsValidationException` | แจ้ง error ไม่ลดแบบเงียบ ๆ |
| ลูกค้าดู order คนอื่นไม่ได้ | `OrderWebControllerTest.getOrderDetail_otherCustomersOrder_returns403`, `OrderSecurityTest.*OfOtherCustomersOrder_returns403` | 403 |
| ลูกค้าเปลี่ยนสถานะ/บันทึกชำระเงินเองไม่ได้ | `OrderSecurityTest.changeStatus_asCustomer_returns403`, `markPaid_asCustomer_returns403` | 403 |
| เปลี่ยนสถานะผิดลำดับ | `OrderStatusServiceTest.invalidTransitionIsRejected` | 409 ไม่บันทึก ไม่ส่ง event |
| หน้าสั่งพิมพ์แสดงโปรโมชันได้ (Thymeleaf 3.1) | `OrderWebControllerTest.showCreateForm_withActivePromotion_rendersPromotionCode` | 200 แสดงโค้ด ไม่ error 500 |
| ส่ง id บริการเสริมซ้ำ | `OrderCommandServiceTest.createOrder_duplicateAddonIds_chargesAddonOnce` | คิดเงินครั้งเดียว = 32.00 บาท |
| ยกเลิกแล้วคืนเงิน รายงานไม่นับ | `ObserverIntegrationTest.fullOrderFlowTriggersAllObservers` | payment = REFUNDED, ยอดขาย = 0 |
