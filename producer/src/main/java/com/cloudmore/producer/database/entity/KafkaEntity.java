package com.cloudmore.producer.database.entity;

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
@Table(name = "kafka_objects")
@Getter
@Setter
public class KafkaEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "topic", length=64, nullable=false)
    private String topic;

    @Column(name = "kafka_key")
    private int key;

    @Column(name = "message", length=255, nullable=false)
    private String message;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaEntity that = (KafkaEntity) o;
        return id == that.id && key == that.key && Objects.equals(topic, that.topic) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
