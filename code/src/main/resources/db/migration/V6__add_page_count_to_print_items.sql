-- จำนวนหน้าต่อ 1 ชุด (quantity = จำนวนชุด) ใช้คำนวณราคา: ราคา x หน้า x ชุด + บริการเสริม x ชุด
ALTER TABLE print_items
    ADD COLUMN page_count INTEGER NOT NULL DEFAULT 1;

ALTER TABLE print_items
    ADD CONSTRAINT chk_print_items_page_count CHECK (page_count > 0);
