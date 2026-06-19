package com.icici.vendorhealthcheckservice.service;

import com.icici.vendorhealthcheckservice.model.VendorHealthConfig;
import com.icici.vendorhealthcheckservice.model.VendorHealthDetail;
import com.icici.vendorhealthcheckservice.repository.VendorHealthRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class VendorHealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(Vendor HealthCheckService.class);

    private final VendorHealthRepository repository;
    private final AtomicReference<LocalDateTime> lastExecution = new AtomicReference<>();

    public VendorHealthCheckService(VendorHealthRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${vendorhealth.poll.fixed-delay-ms:10000}")
    public void scheduledHealthCheck() {
        VendorHealthConfig config = repository.fetchConfig();
        if (config == null) {
            log.warn("No health config row found. Skipping check.");
            return;
        }
        if (!config.isHealthCheckEnabled()) {
            log.info("Vendor health check is disabled in config.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previous = lastExecution.get();
        if (previous != null) {
            Duration elapsed = Duration.between(previous, now);
            if (elapsed.toMinutes() < config.getHealthCheckIntervalMin()) {
                log.debug("Skipping run. Only {} minute(s) elapsed, interval is {} minute(s).",
                        elapsed.toMinutes(), config.getHealthCheckIntervalMin());
                return;
            }
        }

        lastExecution.set(now);
        processVendors(config, now);
    }

    private void processVendors(VendorHealthConfig config, LocalDateTime now) {
        List<VendorHealthDetail> vendors = repository.fetchAllDetails();
        int updated = 0;

        for (VendorHealthDetail vendor : vendors) {
            if (vendor.getConsecutiveErrorCount() > config.getFailureThresholdCount()) {
                if (!"DOWN".equalsIgnoreCase(vendor.getHealthStatus())) {
                    vendor.setDownSince(vendor.getDownSince() == null ? now : vendor.getDownSince());
                } else if (vendor.getDownSince() == null) {
                    vendor.setDownSince(now);
                }
                repository.markVendorDown(vendor);
                updated++;
                log.info("Vendor {} marked DOWN because consecutive error count {} exceeded threshold {}",
                        vendor.getVendorCd(), vendor.getConsecutiveErrorCount(), config.getFailureThresholdCount());
            }
        }

        log.info("Vendor health check completed. Vendors updated: {}", updated);
    }
}
