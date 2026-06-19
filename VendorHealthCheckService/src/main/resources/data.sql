DELETE FROM TBL_VENDOR_HEALTH_DETAILS;
DELETE FROM TBL_VENDOR_HEALTH_CONFIG;

INSERT INTO TBL_VENDOR_HEALTH_CONFIG (
    FAILURETHRESHOLDCOUNT,
    SUCCESSTHRESHOLDCOUNT,
    HEALTHCHECKINTERVAL_MIN,
    IS_HEALTHCHECK_ENABLED,
    CREATED_BY,
    CREATED_ON,
    UPDATED_BY,
    UPDATED_ON
) VALUES (
    3,
    1,
    1,
    1,
    'system',
    datetime('now'),
    'system',
    datetime('now')
);

INSERT INTO TBL_VENDOR_HEALTH_DETAILS (
    VENDOR_CD,
    HEALTHSTATUS,
    CONSECUTIVEERRORCOUNT,
    CONSECUTIVESUCCESSCOUNT,
    LastFailureTime,
    LastSuccessTime,
    DownSince,
    UpdatedOn
) VALUES
('Airt_TRN_A', 'UP', 40, 0, NULL, NULL, NULL, datetime('now')),
('Vendor_B', 'UP', 2, 0, NULL, NULL, NULL, datetime('now')),
('Vendor_C', 'UP', 5, 0, NULL, NULL, NULL, datetime('now'));
