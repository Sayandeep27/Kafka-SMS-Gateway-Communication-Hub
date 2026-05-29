package com.icici.dedmicroservice.service;

import com.icici.dedmicroservice.dto.DEDMessagePayload;
import com.icici.dedmicroservice.repository.DEDMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DEDMessageService {

    private static final Logger log = LoggerFactory.getLogger(DEDMessageService.class);
    private final DEDMessageRepository repository;

    public DEDMessageService(DEDMessageRepository repository) {
        this.repository = repository;
    }

    public void persist(DEDMessagePayload payload) {
        repository.insert(payload);
        log.info("Inserted DED record for msg_id={}", payload.getMsgId());
    }
}
