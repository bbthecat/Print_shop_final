package com.printflow.dto.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    private Integer pageCount = 1;

    @NotNull(message = "กรุณาระบุจำนวนชุด")
    @Min(value = 1, message = "จำนวนชุดต้องมากกว่าหรือเท่ากับ 1")
    private Integer quantity = 1;

    private List<Long> addonIds = new ArrayList<>();

    private String fileName;

    private String fileUrl;

    private String promotionCode;
}
