package com.cloudmore.interprocess.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MoneyConverterTest {

    @Test
    void convertToCoins() {
        assertEquals(0, MoneyConverter.convertToCoins(0.));
        assertEquals(1000, MoneyConverter.convertToCoins(10.));
        assertEquals(-1000, MoneyConverter.convertToCoins(-10.));
        assertEquals(-1056, MoneyConverter.convertToCoins(-10.56));
    }

    @Test
    void convertFromCoins() {
        assertEquals(0., MoneyConverter.convertFromCoins(0), 0.0001);
        assertEquals(10., MoneyConverter.convertFromCoins(1000), 0.0001);
        assertEquals(-10., MoneyConverter.convertFromCoins(-1000), 0.0001);
        assertEquals(-10.56, MoneyConverter.convertFromCoins(-1056), 0.0001);
    }
}