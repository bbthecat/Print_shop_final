package com.printflow.validation;

import com.printflow.domain.entity.PrintOrder;

public class OrderValidationContext {

    private final PrintOrder order;

    public OrderValidationContext(PrintOrder order) {
        this.order = order;
    }

    public PrintOrder getOrder() {
        return order;
    }
}