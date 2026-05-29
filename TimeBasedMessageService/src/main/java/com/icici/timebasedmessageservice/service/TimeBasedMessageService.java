package com.icici.timebasedmessageservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.icici.timebasedmessageservice.dto.TimeBasedKafkaRequest;
import com.icici.timebasedmessageservice.dto.TimeBasedMessageRecord;
import com.icici.timebasedmessageservice.kafka.TimeBasedKafkaProducer;
import com.icici.timebasedmessageservice.repository.TimeBasedMessageRepository;

@Service
public class TimeBasedMessageService {

    private static final Logger log = LoggerFactory.getLogger(TimeBasedMessageService.class);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final TimeBasedMessageRepository repository;
    private final TimeBasedKafkaProducer producer;
    private final String kafkaTopic;
    private final boolean runOnce;

    public TimeBasedMessageService(
            TimeBasedMessageRepository repository,
            TimeBasedKafkaProducer producer,
            @Value("${timebased.kafka.topic}") String kafkaTopic,
            @Value("${timebased.demo.run-once:false}") boolean runOnce) {
        this.repository = repository;
        this.producer = producer;
        this.kafkaTopic = kafkaTopic;
        this.runOnce = runOnce;
    }

    @Scheduled(fixedDelayString = "${timebased.poll.fixed-delay-ms}")
    public void pollAndPublish() {
        if (runOnce) {
            return;
        }
        processMessages();
    }

    public void processMessages() {
        List<TimeBasedMessageRecord> records = repository.fetchReadyMessages();
        int published = 0;

        log.info("Fetched {} DB-validated records in batch", records.size());

        for (TimeBasedMessageRecord record : records) {
            try {
                TimeBasedKafkaRequest payload = buildPayload(record);
                producer.publish(payload);
                published++;
            } catch (Exception ex) {
                log.error("Failed to process msg_id={}", record.getMsgId(), ex);
            }
        }
  
        log.info("Polling cycle completed. fetched={}, published={}", records.size(), published);
    }

    private TimeBasedKafkaRequest buildPayload(TimeBasedMessageRecord record) {
        TimeBasedKafkaRequest payload = new TimeBasedKafkaRequest();
        payload.setMsgId(nullToEmpty(record.getMsgId()));
        payload.setDept(nullToEmpty(record.getDeptId()));
        payload.setAppId(nullToEmpty(record.getAppId()));
        payload.setMobile(nullToEmpty(record.getMobileNo()));
        payload.setDeptMsgId(nullToEmpty(record.getDeptMsgId()));
        payload.setMessage(nullToEmpty(record.getMsgText()));
        payload.setFromDatetime(formatDateTime(record.getMsgSendFromDttime()));
        payload.setToDatetime(formatDateTime(record.getMsgSendToDttime()));
        payload.setNoDeliveryTimeFrom(nullToEmpty(record.getNoDelvryFromTime()));
        payload.setNoDeliveryTimeTo(nullToEmpty(record.getNoDelvryToTime()));
        payload.setHttpMode(defaultIfBlank(record.getMsgHttpMode(), "N"));
        payload.setRemarks(nullToEmpty(record.getInfo1()));
        payload.setTrnGenerateTimestamp(formatDateTime(record.getMsgSourceTimestamp()));
        payload.setDuplicateCheck("N");
        payload.setRemarks1(nullToEmpty(record.getInfo2()));
        payload.setRemarks2(nullToEmpty(record.getInfo3()));
        payload.setTopicName(kafkaTopic);
        payload.setDeptIdOld(nullToEmpty(record.getDeptIdOld()));
        payload.setAppIdOld(nullToEmpty(record.getAppIdOld()));
        return payload;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMAT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
