package com.cloudmore.producer.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(value = "price")
public class PriceSettings {

    private int tax;
}
