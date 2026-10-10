package com.printflow.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminAddonForm {

    @NotBlank(message = "กรุณากรอกชื่อบริการเสริม")
    @Size(max = 100, message = "ชื่อบริการเสริมต้องไม่เกิน 100 ตัวอักษร")
    private String name;

    private String description;

    @NotNull(message = "กรุณาระบุราคา")
    @DecimalMin(value = "0.00", inclusive = true, message = "ราคาต้องมากกว่าหรือเท่ากับ 0")
    @Digits(integer = 8, fraction = 2, message = "จำนวนเงินต้องไม่เกิน 8 หลัก และทศนิยมไม่เกิน 2 ตำแหน่ง")
    private BigDecimal price;
}
