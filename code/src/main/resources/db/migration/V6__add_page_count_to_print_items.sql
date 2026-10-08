-- เพิ่มคอลัมน์ page_count ใน print_items
-- DEFAULT 1 เพื่อ backward-compatible กับข้อมูลเดิม
ALTER TABLE print_items
    ADD COLUMN page_count INTEGER NOT NULL DEFAULT 1;
