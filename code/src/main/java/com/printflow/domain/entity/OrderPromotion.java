package com.printflow.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_promotions")
public class OrderPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PrintOrder order;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    protected OrderPromotion() {
    }

    public OrderPromotion(
            PrintOrder order,
            Long promotionId,
            BigDecimal discountAmount
    ) {
        this.order = order;
        this.promotionId = promotionId;
        this.discountAmount = discountAmount;
    }

    public Long getId() {
        return id;
    }

    public PrintOrder getOrder() {
        return order;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}
