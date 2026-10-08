package com.printflow.dto.form;

import com.printflow.domain.enums.PricingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminServiceForm {

    @NotBlank(message = "กรุณากรอกชื่อบริการ")
    @Size(max = 100, message = "ชื่อบริการต้องไม่เกิน 100 ตัวอักษร")
    private String name;

    private String description;

    @NotNull(message = "กรุณาระบุราคาเริ่มต้น")
    @DecimalMin(value = "0.00", inclusive = true, message = "ราคาต้องมากกว่าหรือเท่ากับ 0")
    private BigDecimal basePrice;

    @NotNull(message = "กรุณาเลือกประเภทราคา")
    private PricingType pricingType = PricingType.BLACK_WHITE;
}
