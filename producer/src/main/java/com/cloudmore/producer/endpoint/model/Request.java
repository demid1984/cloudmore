package com.cloudmore.producer.endpoint.model;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

@Data
public class Request {

    @NotEmpty
    private String name;

    @NotEmpty
    private String surname;

    @Positive
    private double wage;

    @NotNull
    private ZonedDateTime eventTime;

}
