package com.icici.vendorhealthcheckservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icici.vendorhealthcheckservice.dto.VendorHealthGatewayRequest;
import com.icici.vendorhealthcheckservice.security.EncryptionService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VendorHealthNotifier {

    private static final Logger log = LoggerFactory.getLogger(VendorHealthNotifier.class);

    private final RestTemplate restTemplate;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;
    private final String apiUrl;

    public VendorHealthNotifier(
            RestTemplate restTemplate,
            EncryptionService encryptionService,
            ObjectMapper objectMapper,
            @Value("${vendorhealth.api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.encryptionService = encryptionService;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
    }

    public String notifyVendorStatus(String vendorCd, String healthStatus) throws Exception {
        VendorHealthGatewayRequest request = new VendorHealthGatewayRequest();
        request.setVendorCd(vendorCd);
        request.setHealthStatus(healthStatus);
        request.setEventTime(LocalDateTime.now().toString());

        String json = objectMapper.writeValueAsString(request);
        String encryptedPayload = encryptionService.encryptPayload(json, vendorCd);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, new HttpEntity<>(encryptedPayload, headers), String.class);
        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        return encryptionService.decryptPayload(responseBody);
    }
}
