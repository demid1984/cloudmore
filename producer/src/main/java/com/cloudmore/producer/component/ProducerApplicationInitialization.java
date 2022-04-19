package com.cloudmore.producer.component;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

public class ProducerApplicationInitialization implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!StringUtils.hasLength(applicationContext.getEnvironment().getProperty("MYSQL_SERVER"))) {
            throw new IllegalStateException("MYSQL_SERVER variable is empty. Please use it for setting mysql database address.");
        }
        if (!StringUtils.hasLength(applicationContext.getEnvironment().getProperty("KAFKA_SERVERS"))) {
            throw new IllegalStateException("KAFKA_SERVERS variable is empty. Please use it for setting kafka address and port.");
        }
    }
}
