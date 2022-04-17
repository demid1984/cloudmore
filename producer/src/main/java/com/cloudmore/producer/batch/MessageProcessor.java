package com.cloudmore.producer.batch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.NonNull;
import com.cloudmore.producer.database.entity.KafkaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MessageProcessor implements ItemProcessor<KafkaEntity, KafkaEntity> {

    private final KafkaTemplate<String, String> template;

    @Override
    public KafkaEntity process(@NonNull KafkaEntity entity) throws Exception {
        var future = template.send(entity.getTopic(), String.valueOf(entity.getKey()), entity.getMessage());
        try {
            template.flush();
            future.get(5, TimeUnit.SECONDS);
            return entity;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            log.error("Cannot send kafka message: objectId {}", entity.getId());
            throw e;
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (RuntimeException e) {
            log.error("System error on producing kafka message: objectId {}", entity.getId());
            throw e;
        }
    }
}
