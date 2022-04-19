package com.cloudmore.consumer.interprocess;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.cloudmore.consumer.service.CustomerService;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomerEventListener {

    private final CustomerService customerService;

    @KafkaListener(topics = "${consumer.topic}", groupId = "${spring.config.name}.customer.${consumer.group-version:v1}")
    public void customerEvent(CustomerKafkaMessageEvent event) {
        if (customerService.isMessageHandled(event)) {
            log.info("Message has already handled: {}", event);
        } else {
            customerService.handleMessage(event);
        }
    }
}
