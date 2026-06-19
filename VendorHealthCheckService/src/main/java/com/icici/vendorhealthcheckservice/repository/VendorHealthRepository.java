package com.icici.vendorhealthcheckservice.repository;

import com.icici.vendorhealthcheckservice.model.VendorHealthConfig;
import com.icici.vendorhealthcheckservice.model.VendorHealthDetail;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class VendorHealthRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String detailsTable;
    private final String configTable;

    public VendorHealthRepository(
            JdbcTemplate jdbcTemplate,
            @Value("${vendorhealth.db.details-table}") String detailsTable,
            @Value("${vendorhealth.db.config-table}") String configTable) {
        this.jdbcTemplate = jdbcTemplate;
        this.detailsTable = validateTableName(detailsTable);
        this.configTable = validateTableName(configTable);
    }

    public VendorHealthConfig fetchConfig() {
        String sql = "SELECT ID, FAILURETHRESHOLDCOUNT, SUCCESSTHRESHOLDCOUNT, HEALTHCHECKINTERVAL_MIN, IS_HEALTHCHECK_ENABLED "
                + "FROM " + configTable + " ORDER BY ID LIMIT 1";
        List<VendorHealthConfig> rows = jdbcTemplate.query(sql, configRowMapper());
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<VendorHealthDetail> fetchAllDetails() {
        String sql = "SELECT ID, VENDOR_CD, HEALTHSTATUS, CONSECUTIVEERRORCOUNT, CONSECUTIVESUCCESSCOUNT, LastFailureTime, LastSuccessTime, DownSince, UpdatedOn "
                + "FROM " + detailsTable + " ORDER BY ID";
        return jdbcTemplate.query(sql, detailRowMapper());
    }

    public int markVendorDown(VendorHealthDetail vendor) {
        String sql = "UPDATE " + detailsTable + " SET HEALTHSTATUS = ?, CONSECUTIVEERRORCOUNT = ?, CONSECUTIVESUCCESSCOUNT = ?, LastFailureTime = ?, DownSince = ?, UpdatedOn = ? "
                + "WHERE ID = ?";
        LocalDateTime now = LocalDateTime.now();
        return jdbcTemplate.update(sql,
                "DOWN",
                0,
                0,
                now.toString(),
                vendor.getDownSince() == null ? now.toString() : vendor.getDownSince().toString(),
                now.toString(),
                vendor.getId());
    }

    public int markVendorUp(VendorHealthDetail vendor) {
        String sql = "UPDATE " + detailsTable + " SET HEALTHSTATUS = ?, CONSECUTIVEERRORCOUNT = ?, CONSECUTIVESUCCESSCOUNT = ?, LastSuccessTime = ?, DownSince = ?, UpdatedOn = ? "
                + "WHERE ID = ?";
        LocalDateTime now = LocalDateTime.now();
        return jdbcTemplate.update(sql,
                "UP",
                vendor.getConsecutiveErrorCount(),
                0,
                now.toString(),
                null,
                now.toString(),
                vendor.getId());
    }

    public int updateSuccessProgress(VendorHealthDetail vendor, int successCount, LocalDateTime lastSuccessTime) {
        String sql = "UPDATE " + detailsTable + " SET CONSECUTIVESUCCESSCOUNT = ?, LastSuccessTime = ?, UpdatedOn = ? WHERE ID = ?";
        LocalDateTime now = LocalDateTime.now();
        return jdbcTemplate.update(sql,
                successCount,
                lastSuccessTime == null ? now.toString() : lastSuccessTime.toString(),
                now.toString(),
                vendor.getId());
    }

    public int resetSuccessProgress(VendorHealthDetail vendor) {
        String sql = "UPDATE " + detailsTable + " SET CONSECUTIVESUCCESSCOUNT = 0, UpdatedOn = ? WHERE ID = ?";
        return jdbcTemplate.update(sql, LocalDateTime.now().toString(), vendor.getId());
    }

    private RowMapper<VendorHealthConfig> configRowMapper() {
        return (rs, rowNum) -> {
            VendorHealthConfig config = new VendorHealthConfig();
            config.setId(rs.getLong("ID"));
            config.setFailureThresholdCount(rs.getInt("FAILURETHRESHOLDCOUNT"));
            config.setSuccessThresholdCount(rs.getInt("SUCCESSTHRESHOLDCOUNT"));
            config.setHealthCheckIntervalMin(rs.getInt("HEALTHCHECKINTERVAL_MIN"));
            config.setHealthCheckEnabled(rs.getInt("IS_HEALTHCHECK_ENABLED") == 1);
            return config;
        };
    }

    private RowMapper<VendorHealthDetail> detailRowMapper() {
        return (rs, rowNum) -> {
            VendorHealthDetail detail = new VendorHealthDetail();
            detail.setId(rs.getLong("ID"));
            detail.setVendorCd(rs.getString("VENDOR_CD"));
            detail.setHealthStatus(rs.getString("HEALTHSTATUS"));
            detail.setConsecutiveErrorCount(rs.getInt("CONSECUTIVEERRORCOUNT"));
            detail.setConsecutiveSuccessCount(rs.getInt("CONSECUTIVESUCCESSCOUNT"));
            detail.setLastFailureTime(toLocalDateTime(rs, "LastFailureTime"));
            detail.setLastSuccessTime(toLocalDateTime(rs, "LastSuccessTime"));
            detail.setDownSince(toLocalDateTime(rs, "DownSince"));
            detail.setUpdatedOn(toLocalDateTime(rs, "UpdatedOn"));
            return detail;
        };
    }

    private LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        if (timestamp != null) {
            return timestamp.toLocalDateTime();
        }
        String value = rs.getString(column);
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace('T', ' ');
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private String validateTableName(String candidate) {
        if (candidate == null || !candidate.matches("[A-Za-z0-9_\\.]+")) {
            throw new IllegalArgumentException("Invalid table name configured for vendor health service");
        }
        return candidate;
    }
}
