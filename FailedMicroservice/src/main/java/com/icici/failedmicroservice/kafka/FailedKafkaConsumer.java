package com.icici.failedmicroservice.kafka;

import com.icici.failedmicroservice.dto.FailedMessagePayload;
import com.icici.failedmicroservice.service.FailedMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FailedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(FailedKafkaConsumer.class);
    private final FailedMessageService service;

    public FailedKafkaConsumer(FailedMessageService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${failed.kafka.topic}", groupId = "${failed.kafka.group-id}", containerFactory = "failedKafkaListenerContainerFactory")
    public void consume(FailedMessagePayload payload) {
        log.info("Received failed message for msg_id={}", payload.getMsgId());
        service.persist(payload);
    }
}
