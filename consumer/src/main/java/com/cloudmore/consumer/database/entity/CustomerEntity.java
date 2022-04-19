package com.cloudmore.consumer.database.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Getter
@Setter
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "operation_id", length = 36, nullable = false)
    private String operationId;

    @Column(name = "name", length = 255, nullable=false)
    private String name;
    @Column(name = "surname", length = 255, nullable=false)
    private String surname;
    @Column(name = "price", nullable=false)
    private double price;
    @Column(name = "eventTime", nullable=false, columnDefinition = "TIMESTAMP")
    private LocalDateTime eventTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerEntity that = (CustomerEntity) o;
        return id == that.id && Double.compare(that.price, price) == 0 && Objects.equals(operationId, that.operationId) && Objects.equals(name, that.name) && Objects.equals(surname, that.surname) && Objects.equals(eventTime, that.eventTime);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
