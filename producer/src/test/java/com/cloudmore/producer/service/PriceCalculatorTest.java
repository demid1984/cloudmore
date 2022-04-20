package com.cloudmore.producer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import com.cloudmore.producer.settings.PriceSettings;

@SpringJUnitConfig
class PriceCalculatorTest {

    private static final int TAX = 10;

    @Mock
    private PriceSettings priceSettings;

    private PriceCalculator victim;

    @BeforeEach
    public void setup() {
        victim = new PriceCalculator(priceSettings);
        doReturn(TAX).when(priceSettings).getTax();
    }

    @Test
    void test_calculateWithTax() {
        double value = 100.;
        var withTax = victim.calculateWithTax(value);
        assertEquals(value + value / TAX, withTax, 0.001);
        assertEquals(0., victim.calculateWithTax(0.), 0.0001);
    }

}