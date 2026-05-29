package com.icici.timebasedmessageservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

@Configuration                                                                                                                                                                                
public class KafkaConfig {
-
    @Value("${timebased.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${timebased.kafka.mock-enabled:false}")
    private boolean mockEnabled;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        if (mockEnabled) {
            return new ProducerFactory<>() {
                @Override
                public org.apache.kafka.clients.producer.Producer<String, String> createProducer() {
                    return new MockProducer<>(true, new StringSerializer(), new StringSerializer());
                }
            }; 
        }
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
