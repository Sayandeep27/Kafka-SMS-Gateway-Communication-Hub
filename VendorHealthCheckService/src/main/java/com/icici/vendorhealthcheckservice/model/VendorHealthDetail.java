package com.icici.vendorhealthcheckservice.model;

import java.time.LocalDateTime;

public class VendorHealthDetail {
    private long id;
    private String vendorCd;
    private String healthStatus;
    private int consecutiveErrorCount;
    private int consecutiveSuccessCount;
    private LocalDateTime lastFailureTime;
    private LocalDateTime lastSuccessTime;
    private LocalDateTime downSince;
    private LocalDateTime updatedOn;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getVendorCd() { return vendorCd; }
    public void setVendorCd(String vendorCd) { this.vendorCd = vendorCd; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public int getConsecutiveErrorCount() { return consecutiveErrorCount; }
    public void setConsecutiveErrorCount(int consecutiveErrorCount) { this.consecutiveErrorCount = consecutiveErrorCount; }
    public int getConsecutiveSuccessCount() { return consecutiveSuccessCount; }
    public void setConsecutiveSuccessCount(int consecutiveSuccessCount) { this.consecutiveSuccessCount = consecutiveSuccessCount; }
    public LocalDateTime getLastFailureTime() { return lastFailureTime; }
    public void setLastFailureTime(LocalDateTime lastFailureTime) { this.lastFailureTime = lastFailureTime; }
    public LocalDateTime getLastSuccessTime() { return lastSuccessTime; }
    public void setLastSuccessTime(LocalDateTime lastSuccessTime) { this.lastSuccessTime = lastSuccessTime; }
    public LocalDateTime getDownSince() { return downSince; }
    public void setDownSince(LocalDateTime downSince) { this.downSince = downSince; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}
