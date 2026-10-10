-- Orders
CREATE TABLE print_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_price NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_print_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_print_orders_user_id
    ON print_orders(user_id);

CREATE INDEX idx_print_orders_status
    ON print_orders(status);

CREATE INDEX idx_print_orders_created_at
    ON print_orders(created_at);


-- Order Items
CREATE TABLE print_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_print_items_order
        FOREIGN KEY (order_id)
        REFERENCES print_orders(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_print_items_order_id
    ON print_items(order_id);


-- Item Addons
CREATE TABLE print_item_addons (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    addon_id BIGINT NOT NULL,
    price NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_print_item_addons_item
        FOREIGN KEY (item_id)
        REFERENCES print_items(id)
        ON DELETE CASCADE
);


-- Order Promotions
CREATE TABLE order_promotions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    promotion_id BIGINT NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_order_promotions_order
        FOREIGN KEY (order_id)
        REFERENCES print_orders(id)
        ON DELETE CASCADE
);


-- Order Files
CREATE TABLE order_files (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT,

    CONSTRAINT fk_order_files_order
        FOREIGN KEY (order_id)
        REFERENCES print_orders(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_order_files_order_id
    ON order_files(order_id);