package com.icici.successmicroservice.service;

import com.icici.successmicroservice.dto.SuccessMessagePayload;
import com.icici.successmicroservice.repository.SuccessMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SuccessMessageService {

    private static final Logger log = LoggerFactory.getLogger(SuccessMessageService.class);
    private final SuccessMessageRepository repository;

    public SuccessMessageService(SuccessMessageRepository repository) {
        this.repository = repository;
    }

    public void persist(SuccessMessagePayload payload) {
        repository.insert(payload);
        log.info("Inserted success record for msg_id={}", payload.getMsgId());
    }
}
