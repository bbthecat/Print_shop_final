package com.printflow.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "print_items")
public class PrintItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PrintOrder order;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    // จำนวนหน้าต่อ 1 ชุด
    @Column(name = "page_count", nullable = false)
    private Integer pageCount;

    // จำนวนชุด
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected PrintItem() {
    }

    public PrintItem(
            PrintOrder order,
            Long serviceId,
            Integer pageCount,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        this.order = order;
        this.serviceId = serviceId;
        this.pageCount = pageCount;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public PrintOrder getOrder() {
        return order;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}