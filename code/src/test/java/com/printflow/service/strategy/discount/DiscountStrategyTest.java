package com.printflow.service.strategy.discount;

import com.printflow.domain.entity.Promotion;
import com.printflow.domain.enums.DiscountType;
import com.printflow.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountStrategyTest {

    private PercentageDiscountStrategy percentageStrategy;
    private FixedAmountDiscountStrategy fixedAmountStrategy;
    private DiscountStrategyResolver resolver;

    @BeforeEach
    void setUp() {
        percentageStrategy = new PercentageDiscountStrategy();
        fixedAmountStrategy = new FixedAmountDiscountStrategy();
        resolver = new DiscountStrategyResolver(List.of(percentageStrategy, fixedAmountStrategy));
    }

    private Promotion createPromotion(String code, DiscountType type, BigDecimal value, BigDecimal minAmount) {
        Promotion p = new Promotion();
        p.setCode(code);
        p.setDiscountType(type);
        p.setDiscountValue(value);
        p.setMinOrderAmount(minAmount != null ? minAmount : BigDecimal.ZERO);
        p.setActive(true);
        p.setStartDate(LocalDateTime.now().minusDays(1));
        p.setEndDate(LocalDateTime.now().plusDays(10));
        return p;
    }

    @Nested
    @DisplayName("PercentageDiscountStrategy Tests")
    class PercentageTests {
        @Test
        @DisplayName("คำนวณส่วนลดตามเปอร์เซ็นต์ถูกต้อง")
        void calculateNormal() {
            Promotion promo = createPromotion("PERCENT10", DiscountType.PERCENTAGE, new BigDecimal("10.00"), new BigDecimal("100.00"));
            BigDecimal discount = percentageStrategy.calculate(new BigDecimal("200.00"), promo);

            // 10% ของ 200 = 20.00
            assertThat(discount).isEqualByComparingTo(new BigDecimal("20.00"));
        }

        @Test
        @DisplayName("ไม่ถึงยอดสั่งซื้อขั้นต่ำ -> คืนส่วนลด 0.00")
        void belowMinimumOrderAmount() {
            Promotion promo = createPromotion("PERCENT10", DiscountType.PERCENTAGE, new BigDecimal("10.00"), new BigDecimal("100.00"));
            BigDecimal discount = percentageStrategy.calculate(new BigDecimal("80.00"), promo);

            assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("ส่วนลดเกินราคา -> จำกัดส่วนลดไม่ให้เกิน Subtotal (ราคาสุทธิไม่ติดลบ)")
        void discountExceedsSubtotal() {
            Promotion promo = createPromotion("OVER100", DiscountType.PERCENTAGE, new BigDecimal("120.00"), BigDecimal.ZERO);
            BigDecimal discount = percentageStrategy.calculate(new BigDecimal("100.00"), promo);

            assertThat(discount).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("LSP: ข้อมูลไม่ถูกต้อง คืนค่า 0 ไม่ติดลบและไม่ throw exception")
        void edgeCases() {
            Promotion promo = createPromotion("PERCENT10", DiscountType.PERCENTAGE, new BigDecimal("10.00"), BigDecimal.ZERO);
            assertThat(percentageStrategy.calculate(null, promo)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(percentageStrategy.calculate(new BigDecimal("-50.00"), promo)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(percentageStrategy.calculate(new BigDecimal("100.00"), null)).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("คืนประเภท DiscountType.PERCENTAGE ถูกต้อง")
        void correctType() {
            assertThat(percentageStrategy.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
        }
    }

    @Nested
    @DisplayName("FixedAmountDiscountStrategy Tests")
    class FixedAmountTests {
        @Test
        @DisplayName("คำนวณส่วนลดแบบลดเงินคงที่ถูกต้อง")
        void calculateNormal() {
            Promotion promo = createPromotion("SAVE30", DiscountType.FIXED_AMOUNT, new BigDecimal("30.00"), new BigDecimal("100.00"));
            BigDecimal discount = fixedAmountStrategy.calculate(new BigDecimal("200.00"), promo);

            // ลด 30 บาท
            assertThat(discount).isEqualByComparingTo(new BigDecimal("30.00"));
        }

        @Test
        @DisplayName("ไม่ถึงยอดสั่งซื้อขั้นต่ำ -> คืนส่วนลด 0.00")
        void belowMinimumOrderAmount() {
            Promotion promo = createPromotion("SAVE30", DiscountType.FIXED_AMOUNT, new BigDecimal("30.00"), new BigDecimal("200.00"));
            BigDecimal discount = fixedAmountStrategy.calculate(new BigDecimal("150.00"), promo);

            assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("ส่วนลดเกินราคา -> จำกัดส่วนลดไม่เกิน Subtotal (ราคาสุทธิไม่ติดลบ)")
        void discountExceedsSubtotal() {
            Promotion promo = createPromotion("SAVE50", DiscountType.FIXED_AMOUNT, new BigDecimal("50.00"), BigDecimal.ZERO);
            BigDecimal discount = fixedAmountStrategy.calculate(new BigDecimal("30.00"), promo);

            // ซื้อ 30 ลดได้สูงสุดแค่ 30 ไม่ให้ติดลบ
            assertThat(discount).isEqualByComparingTo(new BigDecimal("30.00"));
        }

        @Test
        @DisplayName("LSP: ข้อมูลไม่ถูกต้อง คืนค่า 0 ไม่ติดลบ")
        void edgeCases() {
            Promotion promo = createPromotion("SAVE30", DiscountType.FIXED_AMOUNT, new BigDecimal("30.00"), BigDecimal.ZERO);
            assertThat(fixedAmountStrategy.calculate(null, promo)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(fixedAmountStrategy.calculate(new BigDecimal("-10.00"), promo)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(fixedAmountStrategy.calculate(new BigDecimal("100.00"), null)).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("คืนประเภท DiscountType.FIXED_AMOUNT ถูกต้อง")
        void correctType() {
            assertThat(fixedAmountStrategy.getDiscountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
        }
    }

    @Nested
    @DisplayName("DiscountStrategyResolver Tests")
    class ResolverTests {
        @Test
        @DisplayName("เลือก DiscountStrategy ได้ถูกต้องตาม DiscountType")
        void resolveCorrectly() {
            assertThat(resolver.resolve(DiscountType.PERCENTAGE)).isInstanceOf(PercentageDiscountStrategy.class);
            assertThat(resolver.resolve(DiscountType.FIXED_AMOUNT)).isInstanceOf(FixedAmountDiscountStrategy.class);
        }

        @Test
        @DisplayName("โยน ValidationException เมื่อ discountType เป็น null")
        void resolveNullThrowsException() {
            assertThatThrownBy(() -> resolver.resolve(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Discount type must not be null");
        }
    }
}
