package com.printflow.dto.form;

import com.printflow.domain.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdminPromotionForm {

    @NotBlank(message = "กรุณากรอกรหัสโปรโมชัน")
    @Size(max = 50, message = "รหัสโปรโมชันต้องไม่เกิน 50 ตัวอักษร")
    private String code;

    private String description;

    @NotNull(message = "กรุณาเลือกประเภทส่วนลด")
    private DiscountType discountType = DiscountType.PERCENTAGE;

    @NotNull(message = "กรุณาระบุมูลค่าส่วนลด")
    @DecimalMin(value = "0.01", message = "มูลค่าส่วนลดต้องมากกว่า 0")
    @Digits(integer = 8, fraction = 2, message = "จำนวนเงินต้องไม่เกิน 8 หลัก และทศนิยมไม่เกิน 2 ตำแหน่ง")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", inclusive = true, message = "ยอดสั่งซื้อขั้นต่ำต้องมากกว่าหรือเท่ากับ 0")
    @Digits(integer = 8, fraction = 2, message = "จำนวนเงินต้องไม่เกิน 8 หลัก และทศนิยมไม่เกิน 2 ตำแหน่ง")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @NotNull(message = "กรุณาระบุวันเริ่มต้น")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @NotNull(message = "กรุณาระบุวันสิ้นสุด")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @AssertTrue(message = "ส่วนลดแบบเปอร์เซ็นต์ต้องไม่เกิน 100%")
    public boolean isDiscountValueValid() {
        if (discountType == DiscountType.PERCENTAGE && discountValue != null) {
            return discountValue.compareTo(new BigDecimal("100")) <= 0;
        }
        return true;
    }
}
