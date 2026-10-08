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

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "page_count", nullable = false)
    private Integer pageCount;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected PrintItem() {
    }

    public PrintItem(
            PrintOrder order,
            Long serviceId,
            Integer quantity,
            Integer pageCount,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        this.order = order;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.pageCount = pageCount;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    /** Backward-compat constructor (pageCount defaults to 1) */
    public PrintItem(
            PrintOrder order,
            Long serviceId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        this(order, serviceId, quantity, 1, unitPrice, subtotal);
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

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getPageCount() {
        return pageCount;
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

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
