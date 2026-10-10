-- V3 ยังไม่ได้ผูก FK จากตารางฝั่ง order ไปยังตารางบริการและโปรโมชัน
-- เพิ่มให้ครบตาม ER Diagram เพื่อกันไม่ให้มีรายการที่อ้างถึงบริการ/โปรโมชันที่ไม่มีอยู่จริง
-- ไม่ใส่ ON DELETE CASCADE: ห้ามลบบริการหรือโปรโมชันที่ถูกใช้ในคำสั่งซื้อแล้ว (ให้ปิดใช้งานแทน)
ALTER TABLE print_items
    ADD CONSTRAINT fk_print_items_service
        FOREIGN KEY (service_id) REFERENCES print_services(id);

ALTER TABLE print_item_addons
    ADD CONSTRAINT fk_print_item_addons_addon
        FOREIGN KEY (addon_id) REFERENCES addon_services(id);

ALTER TABLE order_promotions
    ADD CONSTRAINT fk_order_promotions_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions(id);
