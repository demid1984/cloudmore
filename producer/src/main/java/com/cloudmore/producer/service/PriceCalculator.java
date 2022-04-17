package com.cloudmore.producer.service;

import org.springframework.stereotype.Component;
import com.cloudmore.producer.settings.PriceSettings;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class PriceCalculator {

    private final PriceSettings settings;

    public double calculateWithTax(double amount) {
        return amount * (100 + settings.getTax()) / 100.;
    }
}
