package com.cloudmore.producer.endpoint.model;

import java.time.ZonedDateTime;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
    @Min(1)
    @Max(100)
    private double wage;

    @NotNull
    private ZonedDateTime eventTime;

}
