package com.printflow.service.strategy.discount;

import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.FIXED_AMOUNT;
    }

    @Override
    public BigDecimal calculate(BigDecimal subtotal, Promotion promotion) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0 || promotion == null) {
            return BigDecimal.ZERO;
        }

        // ยอดไม่ถึงขั้นต่ำ -> คืนค่าส่วนลด 0
        // ตอนสั่งงานจริง OrderCommandServiceImpl ตรวจขั้นต่ำและแจ้ง error ก่อนแล้ว
        // เช็กตรงนี้ไว้กันพลาด เผื่อ Strategy ถูกเรียกจากที่อื่นโดยไม่ผ่าน Service
        if (promotion.getMinOrderAmount() != null && subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        if (promotion.getDiscountValue() == null || promotion.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = promotion.getDiscountValue().setScale(2, RoundingMode.HALF_UP);

        // ส่วนลดต้องไม่เกินยอดรวม (ราคาสุทธิไม่ติดลบ)
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount;
    }
}
