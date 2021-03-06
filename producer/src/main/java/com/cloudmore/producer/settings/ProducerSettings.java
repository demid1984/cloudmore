package com.cloudmore.producer.settings;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(value = "producer")
public class ProducerSettings {

    @NotEmpty
    private String topic;

}
