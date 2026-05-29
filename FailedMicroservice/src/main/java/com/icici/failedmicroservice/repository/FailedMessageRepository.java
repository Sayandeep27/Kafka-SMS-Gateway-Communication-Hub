package com.icici.failedmicroservice.repository;

import com.icici.failedmicroservice.dto.FailedMessagePayload;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FailedMessageRepository {

    private final JdbcTemplate jdbcTemplate;

    public FailedMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(FailedMessagePayload payload) {
        jdbcTemplate.update(
                "INSERT INTO FAILED_MESSAGES (MSG_ID, DEPT_ID, APP_ID, MOBILE_NO, PIN, POP_MAIL_ID, POP_SENDER_ADDR, TSG_DETAILS, CHANNEL, VENDOR_CD, MSG_TEXT, SEND_COUNT, TOPIC_NAME, DEPT_ID_OLD, APP_ID_OLD) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                payload.getMsgId(), payload.getDeptId(), payload.getAppId(), payload.getMobileNo(),
                payload.getPin(), payload.getPopMailId(), payload.getPopSenderAddr(), payload.getTsgDetails(),
                payload.getChannel(), payload.getVendorCd(), payload.getMsgText(), payload.getSendCount(),
                payload.getTopicName(), payload.getDeptIdOld(), payload.getAppIdOld()
        );
    }
}
