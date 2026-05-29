package com.icici.dedmicroservice.repository;

import com.icici.dedmicroservice.dto.DEDMessagePayload;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DEDMessageRepository {

    private final JdbcTemplate jdbcTemplate;

    public DEDMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(DEDMessagePayload payload) {
        jdbcTemplate.update(
                "INSERT INTO DED_MESSAGES (MSG_ID, DEPT_ID, APP_ID, MOBILE_NO, ERR_DESC, ERROR_SOURCE, ERR_DTTIME, ERR_STACK_TRACE, ERR_VENDOR_CODE, REQUEST_PARAM, TSG_DETAILS, CHANNEL, VENDOR_CD, TOPIC_NAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                payload.getMsgId(), payload.getDeptId(), payload.getAppId(), payload.getMobileNo(),
                payload.getErrDesc(), payload.getErrorSource(), payload.getErrDttime(), payload.getErrStackTrace(),
                payload.getErrVendorCode(), payload.getRequestParam(), payload.getTsgDetails(),
                payload.getChannel(), payload.getVendorCd(), payload.getTopicName()
        );
    }
}
