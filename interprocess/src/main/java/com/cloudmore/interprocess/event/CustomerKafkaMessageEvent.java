package com.cloudmore.interprocess.event;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerKafkaMessageEvent {

    private String operationId;
    private String name;
    private String surname;
    private long amount;
    private ZonedDateTime eventTime;

}
