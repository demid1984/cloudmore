package com.cloudmore.producer.batch;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import com.cloudmore.producer.lock.LockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStarter implements ApplicationListener<ContextRefreshedEvent> {

    private final JobLauncher jobLauncher;
    private final Job kafkaMessageJob;
    private final LockService lockService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        var executor = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder()
                .namingPattern("kafka-message-thread-%d")
                .build());
        executor.scheduleWithFixedDelay(this::run, 0, 2, TimeUnit.SECONDS);
    }

    public void run() {
        try (var lock = lockService.tryLock(kafkaMessageJob.getName())) {
            JobParameters params = new JobParametersBuilder()
                    .addString("JobID", String.valueOf(Instant.now().toEpochMilli()))
                    .toJobParameters();
            jobLauncher.run(kafkaMessageJob, params);
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("Kafka job already running", e);
        } catch (JobRestartException e) {
            log.warn("Kafka job restart", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("Kafka job instance already completed", e);
        } catch (JobParametersInvalidException e) {
            log.warn("Kafka job parameters invalid", e);
        }
    }
}
