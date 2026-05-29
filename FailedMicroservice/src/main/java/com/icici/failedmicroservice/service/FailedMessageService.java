package com.icici.failedmicroservice.service;

import com.icici.failedmicroservice.dto.FailedMessagePayload;
import com.icici.failedmicroservice.repository.FailedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FailedMessageService {

    private static final Logger log = LoggerFactory.getLogger(FailedMessageService.class);
    private final FailedMessageRepository repository;

    public FailedMessageService(FailedMessageRepository repository) {
        this.repository = repository;
    }

    public void persist(FailedMessagePayload payload) {
        repository.insert(payload);
        log.info("Inserted failed record for msg_id={}", payload.getMsgId());
    }
}
