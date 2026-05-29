package com.icici.timebasedmessageservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icici.timebasedmessageservice.dto.TimeBasedKafkaRequest;

@Component
public class TimeBasedKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(TimeBasedKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final boolean mockEnabled;

    public TimeBasedKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${timebased.kafka.mock-enabled:false}") boolean mockEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.mockEnabled = mockEnabled;
    }

    public void publish(TimeBasedKafkaRequest payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(payload.getTopicName(), json);
            if (mockEnabled) {
                log.info("Mock-published v  msg_id={} to topic={} payload={}", payload.getMsgId(), payload.getTopicName(), json);
            } else {
                log.info("Published msg_id={} to topic={}", payload.getMsgId(), payload.getTopicName());
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize Kafka payload for msg_id=" + payload.getMsgId(), ex);
        }
    }
}
