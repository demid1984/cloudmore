package com.cloudmore.producer.interprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import com.cloudmore.producer.database.entity.KafkaEntity;
import com.cloudmore.producer.database.repository.KafkaObjectRepository;
import com.cloudmore.producer.settings.ProducerSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig
class MessageSenderTest {

    private static final String TOPIC = "topic";
    private static final String OBJECT_STRING = "{}";

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private KafkaObjectRepository repository;
    @Mock
    private ProducerSettings producerSettings;
    @Captor
    private ArgumentCaptor<KafkaEntity> argumentCaptor;

    private MessageSender victim;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        victim = new MessageSender(objectMapper, repository, producerSettings);
        doReturn(TOPIC).when(producerSettings).getTopic();
        doReturn(OBJECT_STRING).when(objectMapper).writeValueAsString(any());
    }

    @Test
    void test_send() throws JsonProcessingException {
        var event = new CustomerKafkaMessageEvent();
        event.setName("Test");
        event.setSurname("Testov");
        event.setAmount(10000);
        event.setEventTime(ZonedDateTime.now());
        event.setOperationId(UUID.randomUUID().toString());

        victim.send(event);

        verify(repository).save(argumentCaptor.capture());
        var entity = argumentCaptor.getValue();
        assertEquals(OBJECT_STRING, entity.getMessage());
        assertEquals(TOPIC, entity.getTopic());
        assertTrue(entity.getKey() > 0);

        event.setSurname("Testovich");
        event.setOperationId(UUID.randomUUID().toString());

        victim.send(event);

        verify(repository, times(2)).save(argumentCaptor.capture());
        var entity2 = argumentCaptor.getValue();
        assertNotEquals(entity.getKey(), entity2.getKey());
    }

}