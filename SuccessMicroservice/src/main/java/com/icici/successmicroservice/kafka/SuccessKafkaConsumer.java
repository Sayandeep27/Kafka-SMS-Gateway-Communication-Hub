package com.icici.successmicroservice.kafka;

import com.icici.successmicroservice.dto.SuccessMessagePayload;
import com.icici.successmicroservice.service.SuccessMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SuccessKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(SuccessKafkaConsumer.class);
    private final SuccessMessageService service;

    public SuccessKafkaConsumer(SuccessMessageService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${success.kafka.topic}", groupId = "${success.kafka.group-id}", containerFactory = "successKafkaListenerContainerFactory")
    public void consume(SuccessMessagePayload payload) {
        log.info("Received success message for msg_id={}", payload.getMsgId());
        service.persist(payload);
    }
}
