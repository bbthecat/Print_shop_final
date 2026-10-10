package com.printflow.dto.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderCreateForm {

    @NotNull(message = "กรุณาเลือกบริการงานพิมพ์")
    private Long serviceId;

    @NotNull(message = "กรุณาระบุจำนวนหน้า")
    @Min(value = 1, message = "จำนวนหน้าต้องมากกว่าหรือเท่ากับ 1")
    @Max(value = 2000, message = "จำนวนหน้าต้องไม่เกิน 2,000 หน้าต่อชุด")
    private Integer pageCount = 1;

    @NotNull(message = "กรุณาระบุจำนวนชุด")
    @Min(value = 1, message = "จำนวนชุดต้องมากกว่าหรือเท่ากับ 1")
    @Max(value = 1000, message = "จำนวนชุดต้องไม่เกิน 1,000 ชุด")
    private Integer quantity = 1;

    private List<Long> addonIds = new ArrayList<>();

    @Size(max = 255, message = "ชื่อไฟล์ยาวเกินไป")
    private String fileName;

    @Size(max = 500, message = "ลิงก์ไฟล์ยาวเกินไป")
    private String fileUrl;

    private String promotionCode;
}
