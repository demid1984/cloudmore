package com.cloudmore.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import com.cloudmore.consumer.database.CustomerMessageRepository;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import com.cloudmore.interprocess.utils.MoneyConverter;

@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = ConsumerApplicationTests.DockerDataSourceInitializer.class)
class ConsumerApplicationTests {

    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:5.7");
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka").withTag("5.4.3"))
            .withEmbeddedZookeeper()
            .withExposedPorts(9093);
    private static final String KAFKA_TOPIC = "topic";
    static {
        Stream.of(mysqlContainer, kafkaContainer)
                .parallel()
                .forEach(GenericContainer::start);
        createKafkaTopic(KAFKA_TOPIC);
    }

    static void createKafkaTopic(String kafkaTopic) {
        Integer mappedPort = kafkaContainer.getFirstMappedPort();
        var kafkaBrocker = String.format("%s:%d", kafkaContainer.getHost(), mappedPort);
        try (var admin = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrocker))) {
            admin.createTopics(List.of(new NewTopic(kafkaTopic, 1, (short) 1)));
        }
    }

    private static final ZonedDateTime NOW = LocalDate.of(2022, 4, 15).atStartOfDay(ZoneId.systemDefault());

    public static class DockerDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + mysqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mysqlContainer.getUsername(),
                    "spring.datasource.password=" + mysqlContainer.getPassword(),
                    "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
                    "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                    "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                    "consumer.topic=" + KAFKA_TOPIC
            );
        }
    }

    @Autowired
    CustomerMessageRepository repository;
    @Autowired
    KafkaTemplate<String, CustomerKafkaMessageEvent> template;

    @Test
    void mainTest() throws Exception {
        var event = new CustomerKafkaMessageEvent();
        event.setName("Test");
        event.setSurname("Testov");
        event.setAmount(10000);
        event.setEventTime(NOW);
        event.setOperationId(UUID.randomUUID().toString());

        var future = template.send(KAFKA_TOPIC, event);
        var future2 = template.send(KAFKA_TOPIC, event);
        future.get(3, TimeUnit.SECONDS);
        future2.get(3, TimeUnit.SECONDS);

        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> repository.count() == 1);
        var entity = repository.findAll().get(0);
        assertEquals(event.getOperationId(), entity.getOperationId());
        assertEquals(event.getName(), entity.getName());
        assertEquals(event.getSurname(), entity.getSurname());
        assertEquals(MoneyConverter.convertFromCoins(event.getAmount()), entity.getPrice());
        assertEquals(event.getEventTime().toLocalDateTime(), entity.getEventTime());
    }

}
