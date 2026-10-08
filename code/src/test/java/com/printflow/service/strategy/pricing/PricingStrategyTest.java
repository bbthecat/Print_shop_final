package com.printflow.service.strategy.pricing;

import com.printflow.domain.enums.PricingType;
import com.printflow.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingStrategyTest {

    private BlackWhitePricingStrategy blackWhiteStrategy;
    private ColorPricingStrategy colorStrategy;
    private PhotoPricingStrategy photoStrategy;
    private PricingStrategyResolver resolver;
    private PricingCalculator calculator;

    @BeforeEach
    void setUp() {
        blackWhiteStrategy = new BlackWhitePricingStrategy();
        colorStrategy = new ColorPricingStrategy();
        photoStrategy = new PhotoPricingStrategy();

        List<PricingStrategy> strategies = List.of(blackWhiteStrategy, colorStrategy, photoStrategy);
        resolver = new PricingStrategyResolver(strategies);
        calculator = new PricingCalculator(resolver);
    }

    @Nested
    @DisplayName("BlackWhitePricingStrategy Tests")
    class BlackWhiteTests {
        @Test
        @DisplayName("คำนวณราคางานพิมพ์ขาวดำถูกต้อง")
        void calculateNormal() {
            BigDecimal basePrice = new BigDecimal("1.50");
            BigDecimal total = blackWhiteStrategy.calculate(basePrice, 20, 2);
            // (1.50 * 20) * 2 = 60.00
            assertThat(total).isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("LSP: ราคาต้องไม่ติดลบ คืน 0 เมื่อข้อมูลไม่ถูกต้อง")
        void calculateEdgeCases() {
            assertThat(blackWhiteStrategy.calculate(null, 10, 1)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(blackWhiteStrategy.calculate(new BigDecimal("-1.50"), 10, 1)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(blackWhiteStrategy.calculate(new BigDecimal("1.50"), 0, 1)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(blackWhiteStrategy.calculate(new BigDecimal("1.50"), 10, -1)).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("คืน PricingType ถูกต้อง")
        void correctType() {
            assertThat(blackWhiteStrategy.getPricingType()).isEqualTo(PricingType.BLACK_WHITE);
        }
    }

    @Nested
    @DisplayName("ColorPricingStrategy Tests")
    class ColorTests {
        @Test
        @DisplayName("คำนวณราคางานพิมพ์สีถูกต้อง")
        void calculateNormal() {
            BigDecimal basePrice = new BigDecimal("5.00");
            BigDecimal total = colorStrategy.calculate(basePrice, 10, 3);
            // (5.00 * 10) * 3 = 150.00
            assertThat(total).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("LSP: ราคาต้องไม่ติดลบ คืน 0 เมื่อข้อมูลไม่ถูกต้อง")
        void calculateEdgeCases() {
            assertThat(colorStrategy.calculate(null, 5, 2)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(colorStrategy.calculate(new BigDecimal("-5.00"), 5, 2)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(colorStrategy.calculate(new BigDecimal("5.00"), -1, 2)).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("คืน PricingType ถูกต้อง")
        void correctType() {
            assertThat(colorStrategy.getPricingType()).isEqualTo(PricingType.COLOR);
        }
    }

    @Nested
    @DisplayName("PhotoPricingStrategy Tests")
    class PhotoTests {
        @Test
        @DisplayName("คำนวณราคางานพิมพ์ภาพถ่ายถูกต้อง")
        void calculateNormal() {
            BigDecimal basePrice = new BigDecimal("15.00");
            BigDecimal total = photoStrategy.calculate(basePrice, 5, 1);
            // (15.00 * 5) * 1 = 75.00
            assertThat(total).isEqualByComparingTo(new BigDecimal("75.00"));
        }

        @Test
        @DisplayName("LSP: ราคาต้องไม่ติดลบ คืน 0 เมื่อข้อมูลไม่ถูกต้อง")
        void calculateEdgeCases() {
            assertThat(photoStrategy.calculate(null, 5, 1)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(photoStrategy.calculate(new BigDecimal("-15.00"), 5, 1)).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("คืน PricingType ถูกต้อง")
        void correctType() {
            assertThat(photoStrategy.getPricingType()).isEqualTo(PricingType.PHOTO);
        }
    }

    @Nested
    @DisplayName("PricingStrategyResolver Tests")
    class ResolverTests {
        @Test
        @DisplayName("เลือก Strategy ตาม PricingType ได้ถูกต้อง")
        void resolveCorrectly() {
            assertThat(resolver.resolve(PricingType.BLACK_WHITE)).isInstanceOf(BlackWhitePricingStrategy.class);
            assertThat(resolver.resolve(PricingType.COLOR)).isInstanceOf(ColorPricingStrategy.class);
            assertThat(resolver.resolve(PricingType.PHOTO)).isInstanceOf(PhotoPricingStrategy.class);
        }

        @Test
        @DisplayName("โยน ValidationException เมื่อส่งค่า null")
        void resolveNullThrowsException() {
            assertThatThrownBy(() -> resolver.resolve(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Pricing type must not be null");
        }
    }

    @Nested
    @DisplayName("PricingCalculator Tests")
    class CalculatorTests {
        @Test
        @DisplayName("คำนวณราคากรณีไม่มีบริการเสริม (Addon)")
        void calculateWithoutAddons() {
            BigDecimal basePrice = new BigDecimal("1.50");
            BigDecimal total = calculator.calculateItemTotal(PricingType.BLACK_WHITE, basePrice, 10, 1, List.of());
            // 1.50 * 10 * 1 = 15.00
            assertThat(total).isEqualByComparingTo(new BigDecimal("15.00"));
        }

        @Test
        @DisplayName("คำนวณราคากรณีมีบริการเสริม (Addon) คิดตามจำนวนชุด")
        void calculateWithAddons() {
            BigDecimal basePrice = new BigDecimal("5.00");
            // พิมพ์สี 10 หน้า, 2 ชุด -> 5 * 10 * 2 = 100
            // Addon: เย็บมุม 2 บาท, สันเกลียว 25 บาท -> (2 + 25) * 2 ชุด = 54 บาท
            // รวม = 154 บาท
            List<BigDecimal> addonPrices = List.of(new BigDecimal("2.00"), new BigDecimal("25.00"));
            BigDecimal total = calculator.calculateItemTotal(PricingType.COLOR, basePrice, 10, 2, addonPrices);

            assertThat(total).isEqualByComparingTo(new BigDecimal("154.00"));
        }
    }
}
