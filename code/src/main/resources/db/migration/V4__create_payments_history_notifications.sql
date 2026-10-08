-- Payments
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    amount NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(30),
    payment_status VARCHAR(30) NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES print_orders(id)
        ON DELETE CASCADE
);


-- Order Status Histories
CREATE TABLE order_status_histories (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_status_histories_order
        FOREIGN KEY (order_id)
        REFERENCES print_orders(id),

    CONSTRAINT fk_order_status_histories_user
        FOREIGN KEY (changed_by)
        REFERENCES users(id)
);

CREATE INDEX idx_order_status_histories_order_id
    ON order_status_histories(order_id);


-- Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_notifications_order
        FOREIGN KEY (order_id)
        REFERENCES print_orders(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id_is_read
    ON notifications(user_id, is_read);