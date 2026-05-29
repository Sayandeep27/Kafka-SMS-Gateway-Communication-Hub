package com.icici.successmicroservice.repository;

import com.icici.successmicroservice.dto.SuccessMessagePayload;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SuccessMessageRepository {

    private final JdbcTemplate jdbcTemplate;

    public SuccessMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(SuccessMessagePayload payload) {
        jdbcTemplate.update(
                "INSERT INTO SUCCESS_MESSAGES (MSG_ID, DEPT_ID, APP_ID, MOBILE_NO, MSG_TEXT, PIN, POP_MAIL_ID, POP_SENDER_ADDR, TSG_DETAILS, CHANNEL, VENDOR_CD, SEND_COUNT, TOPIC_NAME, ACK_ID, VENDOR_RESPONSE, DEPT_ID_OLD, APP_ID_OLD) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                payload.getMsgId(), payload.getDeptId(), payload.getAppId(), payload.getMobileNo(), payload.getMsgText(),
                payload.getPin(), payload.getPopMailId(), payload.getPopSenderAddr(), payload.getTsgDetails(),
                payload.getChannel(), payload.getVendorCd(), payload.getSendCount(), payload.getTopicName(),
                payload.getAckId(), payload.getVendorResponse(), payload.getDeptIdOld(), payload.getAppIdOld()
        );
    }
}
