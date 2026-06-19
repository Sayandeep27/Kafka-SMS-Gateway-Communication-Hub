package com.icici.vendorhealthcheckservice.dto;

public class VendorHealthGatewayRequest {
    private String vendorCd;
    private String healthStatus;
    private String eventTime;

    public String getVendorCd() { return vendorCd; }
    public void setVendorCd(String vendorCd) { this.vendorCd = vendorCd; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
}
