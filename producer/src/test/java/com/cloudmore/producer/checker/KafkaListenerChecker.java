package com.cloudmore.producer.checker;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import lombok.Getter;

@Component
@Getter
public class KafkaListenerChecker {

    private CustomerKafkaMessageEvent event;

    @KafkaListener(topics = "${producer.topic}", groupId = "kafka.test.v1")
    public void listen(CustomerKafkaMessageEvent event) {
        this.event = event;
    }

    public boolean isRead() {
        return event != null;
    }
}
