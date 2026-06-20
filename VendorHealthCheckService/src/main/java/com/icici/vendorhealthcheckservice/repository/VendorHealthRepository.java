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
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

@Repository
public class VendorHealthRepository {

    private final SimpleJdbcCall healthCheckConfigDetailsCall;
    private final SimpleJdbcCall markDownCall;
    private final SimpleJdbcCall markUpCall;

    public VendorHealthRepository(DataSource dataSource) {
        this.healthCheckConfigDetailsCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("USP_HEALTHCHECK_CONFIG_DETAILS")
                .returningResultSet("result", new BeanPropertyRowMapper<>(VendorHealthConfig.class))
                .returningResultSet("vendorResult", detailRowMapper());

        this.markDownCall = new SimpleJdbcCall(dataSource) 
                .withProcedureName("USP_VENDOR_HEALTH_MARK_DOWN")
                .declareParameters(
                        new SqlParameter("ID", java.sql.Types.BIGINT),
                        new SqlParameter("DOWN_SINCE", java.sql.Types.TIMESTAMP));

        this.markUpCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("USP_VENDOR_HEALTH_MARK_UP")
                .declareParameters(new SqlParameter("ID", java.sql.Types.BIGINT));
    }

    public VendorHealthConfig fetchConfig() {
        Map<String, Object> result = healthCheckConfigDetailsCall.execute(new MapSqlParameterSource().addValue("FLAG", "GET_CONFIG_DATA"));
        List<VendorHealthConfig> rows = castResult(result.get("result"));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<VendorHealthDetail> fetchDownVendors() {
        Map<String, Object> result = healthCheckConfigDetailsCall.execute(new MapSqlParameterSource().addValue("FLAG", "GET_VENDOR_HEALTH_DATA"));
        return castResult(result.get("vendorResult"));
    }

    public List<VendorHealthDetail> fetchAllVendors() {
        Map<String, Object> result = healthCheckConfigDetailsCall.execute(new MapSqlParameterSource().addValue("FLAG", "GET_ALL_VENDOR_HEALTH_DATA"));
        return castResult(result.get("vendorResult"));
    }

    public void markVendorDown(VendorHealthDetail vendor) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ID", vendor.getId())
                .addValue("DOWN_SINCE", vendor.getDownSince());
        markDownCall.execute(params);
    }

    public void markVendorUp(VendorHealthDetail vendor) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ID", vendor.getId());
        markUpCall.execute(params);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castResult(Object value) {
        return value == null ? List.of() : (List<T>) value;
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
}
