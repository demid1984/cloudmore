package com.cloudmore.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import com.cloudmore.interprocess.utils.MoneyConverter;
import com.cloudmore.producer.checker.KafkaListenerChecker;
import com.cloudmore.producer.database.repository.KafkaObjectRepository;
import com.cloudmore.producer.endpoint.model.Request;
import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(initializers = ProducerApplicationTests.DockerDataSourceInitializer.class)
class ProducerApplicationTests {

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

    private static final int TAX = 10;
    private static final ZonedDateTime NOW = LocalDate.of(2022, 4, 15).atStartOfDay(ZoneId.systemDefault());

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    KafkaObjectRepository repository;
    @Autowired
    KafkaListenerChecker listenerChecker;

    public static class DockerDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + mysqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mysqlContainer.getUsername(),
                    "spring.datasource.password=" + mysqlContainer.getPassword(),
                    "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
                    "spring.kafka.producer.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
                    "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                    "price.tax=" + TAX,
                    "producer.topic=" + KAFKA_TOPIC
            );
        }
    }

    static void createKafkaTopic(String kafkaTopic) {
        Integer mappedPort = kafkaContainer.getFirstMappedPort();
        var kafkaBrocker = String.format("%s:%d", kafkaContainer.getHost(), mappedPort);
        try (var admin = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrocker))) {
            admin.createTopics(List.of(new NewTopic(kafkaTopic, 1, (short) 1)));
        }
    }

    @Test
    void mainTest() throws Exception {

        var request = new Request();
        request.setName("Test");
        request.setSurname("Testov");
        request.setWage(100.);
        request.setEventTime(NOW);

        var builder = MockMvcRequestBuilders.post("/api/customer/receipt")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsBytes(request));
        mockMvc.perform(builder)
                .andExpect(status().isNoContent());
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> repository.findAll().iterator().hasNext());
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> listenerChecker.isRead());
        var event = listenerChecker.getEvent();
        assertEquals(request.getName(), event.getName());
        assertEquals(request.getSurname(), event.getSurname());
        assertEquals(request.getEventTime().withZoneSameInstant(ZoneId.of("UTC")), event.getEventTime());
        assertFalse(event.getOperationId().isEmpty());
        assertEquals(MoneyConverter.convertToCoins(request.getWage() + request.getWage() * TAX / 100.), event.getAmount());
    }

}
