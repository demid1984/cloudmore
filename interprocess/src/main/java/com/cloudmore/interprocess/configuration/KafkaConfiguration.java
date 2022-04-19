package com.cloudmore.interprocess.configuration;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import com.cloudmore.interprocess.event.CustomerKafkaMessageEvent;
import lombok.RequiredArgsConstructor;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration implements BeanPostProcessor {

    private final Map<String, Class<?>> topicClassMap = new HashMap<>();
    private final ConfigurableBeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Stream.of(ReflectionUtils.getDeclaredMethods(bean.getClass()))
                .filter(m -> m.isAnnotationPresent(KafkaListener.class))
                .forEach(m -> {
                    var kafkaTopics = m.getAnnotation(KafkaListener.class).topics();
                    var parameters = m.getParameters();
                    Class<?> kafkaMessageCls;
                    if (parameters.length == 1) {
                        kafkaMessageCls = parameters[0].getType();
                    } else {
                        kafkaMessageCls = Stream.of(m.getParameters())
                                .filter(p -> p.isAnnotationPresent(Payload.class))
                                .findFirst()
                                .map(Parameter::getType)
                                .orElseThrow(() -> new IllegalStateException("Cannot find @Payload annotation in kafka listener"));
                    }
                    Stream.of(kafkaTopics).forEach(t -> topicClassMap.putIfAbsent(beanFactory.resolveEmbeddedValue(t), kafkaMessageCls));
                });
        return bean;
    }

    @Bean
    public Deserializer<Object> deserializer() {
        return new JsonDeserializer<>(Object.class) {
            @Override
            public Object deserialize(String topic, Headers headers, byte[] data) {
                try {
                    var cls = topicClassMap.get(topic);
                    Assert.state(cls != null, "No default type provided for topic " + topic);
                    return objectMapper.readerFor(cls).readValue(data);
                } catch (IOException e) {
                    throw new DeserializationException("Can't deserialize data [" + Arrays.toString(data) + "] from topic [" + topic + "]", data, false, e);
                }
            }

            @Override
            public Object deserialize(String topic, byte[] data) {
                return deserialize(topic, null, data);
            }
        };
    }

    @Bean
    public ConsumerFactory<String, Object> kafkaConsumerFactory(KafkaProperties kafkaProperties,
                                                                Deserializer<Object> deserializer) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new ErrorHandlingDeserializer<>(deserializer));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
                                                                                                 ConsumerFactory<String, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure((ConcurrentKafkaListenerContainerFactory) factory, (ConsumerFactory) kafkaConsumerFactory);
        var backoff = new FixedBackOff(2500, 500);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backoff));
        return factory;

    }

    @Bean
    public MessageHandlerMethodFactory kafkaHandlerMethodFactory(LocalValidatorFactoryBean validatorFactory) {
        var messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setValidator(validatorFactory);
        return messageHandlerMethodFactory;
    }

}
