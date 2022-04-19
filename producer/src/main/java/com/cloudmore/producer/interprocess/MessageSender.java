package com.cloudmore.producer.interprocess;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import com.cloudmore.producer.database.entity.KafkaEntity;
import com.cloudmore.producer.database.repository.KafkaObjectRepository;
import com.cloudmore.producer.settings.ProducerSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MessageSender {

    private final ObjectMapper objectMapper;
    private final KafkaObjectRepository repository;
    private final ProducerSettings producerSettings;

    @Transactional
    public void send(CustomerKafkaMessageEvent message) throws JsonProcessingException {
        var key = String.format("%s %s", message.getName(), message.getSurname()).hashCode();
        var kafkaMessage = objectMapper.writeValueAsString(message);

        var entity = new KafkaEntity();
        entity.setKey(key);
        entity.setMessage(kafkaMessage);
        entity.setTopic(producerSettings.getTopic());

        repository.save(entity);
    }
}
