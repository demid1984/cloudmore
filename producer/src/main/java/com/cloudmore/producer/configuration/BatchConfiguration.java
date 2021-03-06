package com.cloudmore.producer.configuration;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import com.cloudmore.producer.batch.KafkaJobExecutionListener;
import com.cloudmore.producer.batch.MessageProcessor;
import com.cloudmore.producer.database.entity.KafkaEntity;
import com.cloudmore.producer.database.repository.KafkaObjectRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

    @Bean
    public RepositoryItemReader<KafkaEntity> reader(KafkaObjectRepository repository) {
        return new RepositoryItemReaderBuilder<KafkaEntity>()
                .repository(repository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .pageSize(20)
                .methodName("findAll")
                .saveState(false)
                .build();
    }

    @Bean
    public RepositoryItemWriter<KafkaEntity> writer(KafkaObjectRepository repository) {
        return new RepositoryItemWriterBuilder<KafkaEntity>()
                .repository(repository)
                .methodName("delete")
                .build();
    }

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new KafkaJobExecutionListener();
    }

    @Bean
    public Job kafkaMessageJob(JobBuilderFactory jobBuilderFactory,
                               Step kafkaSenderStep,
                               JobExecutionListener listener) {
        return jobBuilderFactory.get("kafkaMessageJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(kafkaSenderStep)
                .end()
                .build();
    }

    @Bean
    public Step kafkaSenderStep(StepBuilderFactory stepBuilderFactory,
                      RepositoryItemReader<KafkaEntity> reader,
                      RepositoryItemWriter<KafkaEntity> writer,
                      MessageProcessor processor) {
        return stepBuilderFactory.get("kafkaSenderStep")
                .<KafkaEntity, KafkaEntity> chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public MessageProcessor processor(KafkaTemplate<String, String> template) {
        return new MessageProcessor(template);
    }

}
