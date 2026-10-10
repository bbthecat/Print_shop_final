package com.printflow.validation;

import com.printflow.domain.entity.OrderFile;
import com.printflow.exception.ValidationException;

import java.util.Set;

public class FileTypeValidationHandler extends OrderValidationHandler {

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    @Override
    protected void validate(OrderValidationContext context) {

        for (OrderFile file : context.getFiles()) {

            if (file.getFileType() == null
                    || !ALLOWED_FILE_TYPES.contains(file.getFileType().toLowerCase())) {

                throw new ValidationException(
                        "รองรับเฉพาะไฟล์ PDF, JPG และ PNG (ไฟล์ที่ส่งมา: " + file.getFileName() + ")"
                );
            }
        }
    }
}