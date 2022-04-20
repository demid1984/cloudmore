package com.cloudmore.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import com.cloudmore.consumer.database.CustomerMessageRepository;
import com.cloudmore.consumer.database.entity.CustomerEntity;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import com.cloudmore.interprocess.utils.MoneyConverter;

@SpringJUnitConfig
class CustomerServiceTest {

    @Mock
    CustomerMessageRepository repository;
    @Captor
    ArgumentCaptor<CustomerEntity> entityCaptor;

    private CustomerService victim;

    @BeforeEach
    void setup() {
        victim = new CustomerService(repository);
    }

    @Test
    void handleMessage() {
        var event = new CustomerKafkaMessageEvent();
        event.setOperationId(UUID.randomUUID().toString());
        event.setName("Test");
        event.setSurname("Testov");
        event.setAmount(10000);
        event.setEventTime(ZonedDateTime.now());

        victim.handleMessage(event);
        verify(repository).save(entityCaptor.capture());
        var entity = entityCaptor.getValue();

        assertEquals(event.getOperationId(), entity.getOperationId());
        assertEquals(event.getName(), entity.getName());
        assertEquals(event.getSurname(), entity.getSurname());
        assertEquals(MoneyConverter.convertFromCoins(event.getAmount()), entity.getPrice());
        assertEquals(event.getEventTime().toLocalDateTime(), entity.getEventTime());
    }

    @Test
    void isMessageHandled() {
        var event = new CustomerKafkaMessageEvent();
        event.setOperationId(UUID.randomUUID().toString());

        doReturn(false).when(repository).existsByOperationId(event.getOperationId());
        assertFalse(victim.isMessageHandled(event));

        doReturn(true).when(repository).existsByOperationId(event.getOperationId());
        assertTrue(victim.isMessageHandled(event));

        event.setOperationId(UUID.randomUUID().toString());
        assertFalse(victim.isMessageHandled(event));
    }
}