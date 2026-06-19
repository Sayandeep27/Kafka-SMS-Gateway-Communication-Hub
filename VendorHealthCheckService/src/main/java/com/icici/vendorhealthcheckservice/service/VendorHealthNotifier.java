package com.icici.vendorhealthcheckservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icici.vendorhealthcheckservice.dto.GatewayRequestBody;
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
    private final String mobileNo;
    private final String senderId;
    private final String message;
    private final String msgId;
    private final String deptId;
    private final String appId;
    private final String smsType;
    private final String channel;

    public VendorHealthNotifier(
            RestTemplate restTemplate,
            EncryptionService encryptionService,
            ObjectMapper objectMapper,
            @Value("${vendorhealth.api.url}") String apiUrl,
            @Value("${vendorhealth.gateway.mobile-no}") String mobileNo,
            @Value("${vendorhealth.gateway.sender-id}") String senderId,
            @Value("${vendorhealth.gateway.message}") String message,
            @Value("${vendorhealth.gateway.msgid}") String msgId,
            @Value("${vendorhealth.gateway.deptid}") String deptId,
            @Value("${vendorhealth.gateway.appid}") String appId,
            @Value("${vendorhealth.gateway.sms-type}") String smsType,
            @Value("${vendorhealth.gateway.channel}") String channel) {
        this.restTemplate = restTemplate;
        this.encryptionService = encryptionService;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.mobileNo = mobileNo;
        this.senderId = senderId;
        this.message = message;
        this.msgId = msgId;
        this.deptId = deptId;
        this.appId = appId;
        this.smsType = smsType;
        this.channel = channel;
    }

    public boolean probeVendorGateway(String vendorCd) throws Exception {
        GatewayRequestBody request = new GatewayRequestBody();
        request.setMobile_no(mobileNo);
        request.setSender_id(senderId);
        request.setMessage(message);
        request.setMsgid(msgId);
        request.setDeptid(deptId);
        request.setAppid(appId);
        request.setSms_TYPE(smsType);
        request.setVendor_cd(vendorCd);
        request.setChannel(channel);

        String json = objectMapper.writeValueAsString(request);
        String encryptedPayload = encryptionService.encryptPayload(json, msgId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, new HttpEntity<>(encryptedPayload, headers), String.class);
        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }
        String decrypted = encryptionService.decryptPayload(responseBody);
        log.info("Gateway probe response for vendor {} -> {}", vendorCd, decrypted);
        return true;
    }
}
