package com.cloudmore.consumer.interprocess;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import com.cloudmore.consumer.service.CustomerService;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;

@SpringJUnitConfig
class CustomerEventListenerTest {

    @Mock
    private CustomerService customerService;

    private CustomerEventListener victim;

    @BeforeEach
    public void setup() {
        victim = new CustomerEventListener(customerService);
    }

    @Test
    void customerEventFirstMessage() {
        var event = new CustomerKafkaMessageEvent();
        event.setOperationId(UUID.randomUUID().toString());

        doReturn(false).when(customerService).isMessageHandled(event);
        victim.customerEvent(event);
        verify(customerService).handleMessage(event);
    }

    @Test
    void customerEventTwiceMessage() {
        var event = new CustomerKafkaMessageEvent();
        event.setOperationId(UUID.randomUUID().toString());

        doReturn(true).when(customerService).isMessageHandled(event);
        victim.customerEvent(event);
        verify(customerService, never()).handleMessage(any());
    }
}