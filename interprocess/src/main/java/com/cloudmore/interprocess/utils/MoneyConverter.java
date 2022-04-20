package com.cloudmore.interprocess.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoneyConverter {

    private static final long MULTIPLIER = 100;

    public static long convertToCoins(double value) {
        return (long)(MULTIPLIER * value);
    }

    public static double convertFromCoins(long value) {
        return value * 1. / MULTIPLIER;
    }
}
