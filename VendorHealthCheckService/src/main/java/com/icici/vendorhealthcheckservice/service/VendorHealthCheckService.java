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

    private static final Logger log = LoggerFactory.getLogger(VendorHealthCheckService.class);

    private final VendorHealthRepository repository;
    private final AtomicReference<LocalDateTime> lastExecution = new AtomicReference<>();
    private final VendorHealthNotifier notifier;

    public VendorHealthCheckService(VendorHealthRepository repository, VendorHealthNotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
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
        List<VendorHealthDetail> vendors = repository.fetchDownVendors();
        int updated = 0;

        for (VendorHealthDetail vendor : vendors) {
            try {
                boolean success = notifier.probeVendorGateway(vendor.getVendorCd());
                if (success) {
                    int nextSuccessCount = vendor.getConsecutiveSuccessCount() + 1;
                    vendor.setConsecutiveSuccessCount(nextSuccessCount);
                    if (nextSuccessCount >= config.getSuccessThresholdCount()) {
                        repository.markVendorUp(vendor);
                        updated++;
                        log.info("Vendor {} marked UP after {} consecutive successful checks",
                                vendor.getVendorCd(), nextSuccessCount);
                    } else {
                        log.info("Vendor {} remains DOWN. Success streak {}/{}",
                                vendor.getVendorCd(), nextSuccessCount, config.getSuccessThresholdCount());
                    }
                } else {
                    log.info("Vendor {} remains DOWN because API probe returned empty response", vendor.getVendorCd());
                }
            } catch (Exception ex) {
                log.error("Error while processing vendor {}", vendor.getVendorCd(), ex);
            }
        }

        log.info("Vendor health check completed. Vendors updated: {}", updated);
    }
}
