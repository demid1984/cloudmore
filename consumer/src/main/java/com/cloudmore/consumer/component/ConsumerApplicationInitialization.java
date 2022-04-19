package com.cloudmore.consumer.component;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

public class ConsumerApplicationInitialization implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!StringUtils.hasLength(applicationContext.getEnvironment().getProperty("KAFKA_SERVERS"))) {
            throw new IllegalStateException("KAFKA_SERVERS variable is empty. Please use it for setting kafka address and port.");
        }
    }
}
