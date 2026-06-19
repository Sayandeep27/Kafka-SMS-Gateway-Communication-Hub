package com.icici.vendorhealthcheckservice.model;

public class VendorHealthConfig {
    private long id;
    private int failureThresholdCount;
    private int successThresholdCount;
    private int healthCheckIntervalMin;
    private boolean healthCheckEnabled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getFailureThresholdCount() {
        return failureThresholdCount;
    }

    public void setFailureThresholdCount(int failureThresholdCount) {
        this.failureThresholdCount = failureThresholdCount;
    }

    public int getSuccessThresholdCount() {
        return successThresholdCount;
    }

    public void setSuccessThresholdCount(int successThresholdCount) {
        this.successThresholdCount = successThresholdCount;
    }

    public int getHealthCheckIntervalMin() {
        return healthCheckIntervalMin;
    }

    public void setHealthCheckIntervalMin(int healthCheckIntervalMin) {
        this.healthCheckIntervalMin = healthCheckIntervalMin;
    }

    public boolean isHealthCheckEnabled() {
        return healthCheckEnabled;
    }

    public void setHealthCheckEnabled(boolean healthCheckEnabled) {
        this.healthCheckEnabled = healthCheckEnabled;
    }
}
