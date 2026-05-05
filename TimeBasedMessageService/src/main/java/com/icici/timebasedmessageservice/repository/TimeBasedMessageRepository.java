package com.icici.timebasedmessageservice.repository;

import com.icici.timebasedmessageservice.dto.TimeBasedMessageRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TimeBasedMessageRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final String pipeId;
    private final int fetchLimit;

    public TimeBasedMessageRepository(
            JdbcTemplate jdbcTemplate,
            @Value("${timebased.db.table-name}") String tableName,
            @Value("${timebased.db.pipe-id:}") String pipeId,
            @Value("${timebased.db.fetch-limit:1000}") int fetchLimit) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = validateTableName(tableName);
        this.pipeId = pipeId == null ? "" : pipeId.trim();
        this.fetchLimit = fetchLimit;
    }

    public List<TimeBasedMessageRecord> fetchReadyMessages() {
        StringBuilder sql = new StringBuilder()
                .append("SELECT TOP ").append(fetchLimit).append(" ")
                .append("MSG_ID, DEPT_ID, APP_ID, APP_CODE, PARTNER_CODE, PIN, POP_MAIL_ID, POP_SENDER_ADDR, ")
                .append("DEPT_MSG_ID, MOBILE_NO, MSG_TEXT, MSG_SEND_FROM_DTTIME, MSG_SEND_TO_DTTIME, ")
                .append("NO_DELVRY_FROMTIME, NO_DELVRY_TOTIME, MSG_HTTP_MODE, MSG_STTS, RQST_ACK_ID, ")
                .append("RQST_ACK_DTTIME, INFO1, INFO2, INFO3, INFO4, SEND_COUNT, PIPE_ID, DEPT_ID_OLD, APP_ID_OLD, ")
                .append("PRFRRD_CHANNEL, ALT_CHANNEL, ALT_CHANNEL_SEND_FROMDTTIME, ALT_CHANNEL_SEND_TODTTIME, ")
                .append("MSG_SOURCE_TIMESTAMP ")
                .append("FROM ").append(tableName).append(" WITH (NOLOCK) ")
                .append("WHERE MSG_STTS = ?");

        if (!pipeId.isEmpty()) {
            sql.append(" AND PIPE_ID = ?");
        }

        sql.append(" ORDER BY MSG_ID");

        if (!pipeId.isEmpty()) {
            return jdbcTemplate.query(sql.toString(), rowMapper(), "R", pipeId);
        }
        return jdbcTemplate.query(sql.toString(), rowMapper(), "R");
    }

    private RowMapper<TimeBasedMessageRecord> rowMapper() {
        return (rs, rowNum) -> {
            TimeBasedMessageRecord record = new TimeBasedMessageRecord();
            record.setMsgId(rs.getString("MSG_ID"));
            record.setDeptId(rs.getString("DEPT_ID"));
            record.setAppId(rs.getString("APP_ID"));
            record.setAppCode(rs.getString("APP_CODE"));
            record.setPartnerCode(rs.getString("PARTNER_CODE"));
            record.setPin(rs.getString("PIN"));
            record.setPopMailId(rs.getString("POP_MAIL_ID"));
            record.setPopSenderAddr(rs.getString("POP_SENDER_ADDR"));
            record.setDeptMsgId(rs.getString("DEPT_MSG_ID"));
            record.setMobileNo(rs.getString("MOBILE_NO"));
            record.setMsgText(rs.getString("MSG_TEXT"));
            record.setMsgSendFromDttime(toLocalDateTime(rs, "MSG_SEND_FROM_DTTIME"));
            record.setMsgSendToDttime(toLocalDateTime(rs, "MSG_SEND_TO_DTTIME"));
            record.setNoDelvryFromTime(rs.getString("NO_DELVRY_FROMTIME"));
            record.setNoDelvryToTime(rs.getString("NO_DELVRY_TOTIME"));
            record.setMsgHttpMode(rs.getString("MSG_HTTP_MODE"));
            record.setMsgStts(rs.getString("MSG_STTS"));
            record.setRqstAckId(rs.getString("RQST_ACK_ID"));
            record.setRqstAckDttime(toLocalDateTime(rs, "RQST_ACK_DTTIME"));
            record.setInfo1(rs.getString("INFO1"));
            record.setInfo2(rs.getString("INFO2"));
            record.setInfo3(rs.getString("INFO3"));
            record.setInfo4(rs.getString("INFO4"));
            record.setSendCount(rs.getString("SEND_COUNT"));
            record.setPipeId(rs.getString("PIPE_ID"));
            record.setDeptIdOld(rs.getString("DEPT_ID_OLD"));
            record.setAppIdOld(rs.getString("APP_ID_OLD"));
            record.setPrfrrdChannel(rs.getString("PRFRRD_CHANNEL"));
            record.setAltChannel(rs.getString("ALT_CHANNEL"));
            record.setAltChannelSendFromDttime(toLocalDateTime(rs, "ALT_CHANNEL_SEND_FROMDTTIME"));
            record.setAltChannelSendToDttime(toLocalDateTime(rs, "ALT_CHANNEL_SEND_TODTTIME"));
            record.setMsgSourceTimestamp(toLocalDateTime(rs, "MSG_SOURCE_TIMESTAMP"));
            return record;
        };
    }

    private LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String validateTableName(String candidate) {
        if (candidate == null || !candidate.matches("[A-Za-z0-9_\\.]+")) {
            throw new IllegalArgumentException("Invalid table name configured for timebased.db.table-name");
        }
        return candidate;
    }
}
