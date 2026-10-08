package com.printflow.validation;

import com.printflow.domain.entity.OrderFile;
import com.printflow.domain.entity.PrintItem;
import com.printflow.domain.entity.PrintOrder;
import com.printflow.domain.entity.OrderPromotion;

import java.util.List;

public class OrderValidationContext {

    private final PrintOrder order;
    private final List<PrintItem> items;
    private final List<OrderFile> files;
    private final List<OrderPromotion> promotions;

    public OrderValidationContext(
        PrintOrder order,
        List<PrintItem> items,
        List<OrderFile> files,
        List<OrderPromotion> promotions
    ) {
    this.order = order;
    this.items = items;
    this.files = files;
    this.promotions = promotions;
    }

    public PrintOrder getOrder() {
        return order;
    }

    public List<PrintItem> getItems() {
        return items;
    }

    public List<OrderFile> getFiles() {
        return files;
    }

    public List<OrderPromotion> getPromotions() {
    return promotions;
    }
    
}