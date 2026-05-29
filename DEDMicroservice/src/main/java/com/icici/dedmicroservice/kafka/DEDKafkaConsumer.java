package com.icici.dedmicroservice.kafka;

import com.icici.dedmicroservice.dto.DEDMessagePayload;
import com.icici.dedmicroservice.service.DEDMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DEDKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(DEDKafkaConsumer.class);
    private final DEDMessageService service;

    public DEDKafkaConsumer(DEDMessageService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${ded.kafka.topic}", groupId = "${ded.kafka.group-id}", containerFactory = "dedKafkaListenerContainerFactory")
    public void consume(DEDMessagePayload payload) {
        log.info("Received DED message for msg_id={}", payload.getMsgId());
        service.persist(payload);
    }
}
