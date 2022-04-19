package com.cloudmore.consumer.service;

import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cloudmore.consumer.database.CustomerMessageRepository;
import com.cloudmore.consumer.database.entity.CustomerEntity;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import com.cloudmore.interprocess.utils.MoneyConverter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMessageRepository repository;

    @Transactional
    public void handleMessage(CustomerKafkaMessageEvent event) {
        var customerEntity = new CustomerEntity();
        customerEntity.setOperationId(event.getOperationId());
        customerEntity.setName(event.getName());
        customerEntity.setSurname(event.getSurname());
        customerEntity.setPrice(MoneyConverter.convertFromCoins(event.getAmount()));
        customerEntity.setEventTime(event.getEventTime().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
        repository.save(customerEntity);
    }

    public boolean isMessageHandled(CustomerKafkaMessageEvent event) {
        return repository.existsByOperationId(event.getOperationId());
    }
}
