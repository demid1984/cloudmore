package com.cloudmore.producer.database.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import com.cloudmore.producer.database.entity.KafkaEntity;

public interface KafkaObjectRepository extends PagingAndSortingRepository<KafkaEntity, Integer> {
}
