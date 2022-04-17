package com.cloudmore.producer.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(value = "producer")
public class ProducerSettings {

    private String topic;
    private int tax;
}
