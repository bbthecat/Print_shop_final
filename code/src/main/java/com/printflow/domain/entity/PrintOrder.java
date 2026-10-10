package com.printflow.domain.entity;

import com.printflow.domain.enums.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "print_orders",
    indexes = {
        @Index(name = "idx_print_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_print_orders_status", columnList = "status"),
        @Index(name = "idx_print_orders_created_at", columnList = "created_at")
    }
)
public class PrintOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // One-to-Many ฝั่งตรงข้ามของ PrintItem.order (inverse side) ใช้อ่านอย่างเดียว ไม่สร้างคอลัมน์ใหม่
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<PrintItem> items = new ArrayList<>();

    protected PrintOrder() {
    }

    public PrintOrder(
            String orderNumber,
            Long userId,
            OrderStatus status,
            BigDecimal totalPrice
    ) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<PrintItem> getItems() {
        return items;
    }
}
