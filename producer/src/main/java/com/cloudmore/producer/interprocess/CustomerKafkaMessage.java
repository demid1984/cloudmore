package com.cloudmore.producer.interprocess;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerKafkaMessage {

    private UUID operationId;
    private String name;
    private String surname;
    private long amount;
    private Instant eventTime;

}
