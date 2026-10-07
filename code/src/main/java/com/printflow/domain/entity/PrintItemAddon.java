package com.printflow.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "print_item_addons")
public class PrintItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private PrintItem item;

    @Column(name = "addon_id", nullable = false)
    private Long addonId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    protected PrintItemAddon() {
    }

    public PrintItemAddon(
            PrintItem item,
            Long addonId,
            BigDecimal price
    ) {
        this.item = item;
        this.addonId = addonId;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public PrintItem getItem() {
        return item;
    }

    public Long getAddonId() {
        return addonId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
