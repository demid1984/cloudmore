package com.cloudmore.consumer.database;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cloudmore.consumer.database.entity.CustomerEntity;

public interface CustomerMessageRepository extends JpaRepository<CustomerEntity, Integer> {

    boolean existsByOperationId(String operationId);
}
