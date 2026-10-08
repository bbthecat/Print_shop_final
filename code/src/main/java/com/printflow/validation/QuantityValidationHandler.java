package com.printflow.validation;

import com.printflow.domain.entity.PrintItem;
import com.printflow.exception.ValidationException;

public class QuantityValidationHandler extends OrderValidationHandler {

    @Override
    protected void validate(OrderValidationContext context) {

        for (PrintItem item : context.getItems()) {

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ValidationException(
                        "Quantity must be greater than 0"
                );
            }
        }
    }
}