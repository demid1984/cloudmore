package com.cloudmore.consumer.settings;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(value = "consumer")
public class ConsumerSettings {

    @NotEmpty
    private String topic;
    private String groupVersion;
}
