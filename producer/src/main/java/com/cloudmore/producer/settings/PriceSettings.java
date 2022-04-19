package com.cloudmore.producer.settings;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(value = "price")
public class PriceSettings {

    @Min(1)
    @Max(100)
    private int tax;
}
