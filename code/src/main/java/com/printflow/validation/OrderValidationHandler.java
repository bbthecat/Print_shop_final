package com.printflow.validation;

public abstract class OrderValidationHandler {

    private OrderValidationHandler next;

    public OrderValidationHandler setNext(OrderValidationHandler next) {
        this.next = next;
        return next;
    }

    public void handle(OrderValidationContext context) {
        validate(context);

        if (next != null) {
            next.handle(context);
        }
    }

    protected abstract void validate(OrderValidationContext context);
}