package com.icici.vendorhealthcheckservice.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);
    private static final int AES_BLOCK_SIZE = 16;

    @Value("${vendorhealth.crypto.public-key-path:D:\\key\\ICICI_UAT_PublicCert.cer}")
    private String publicKeyPath;

    @Value("${vendorhealth.crypto.private-key-path:D:\\key\\DISPENSER_PrivateKey.pem}")
    private String privateKeyPath;

    public String encryptPayload(String data, String messageID) throws Exception {
        String skey = RandomStringUtils.randomAlphanumeric(16);
        byte[] keyBytes = skey.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedKeyBytes = encryptRSA(keyBytes);
        String encodedEncryptedKey = Base64.getEncoder().encodeToString(encryptedKeyBytes);

        String sIV = RandomStringUtils.randomAlphanumeric(16);
        byte[] ivBytes = sIV.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedContent = encryptAES(data.getBytes(StandardCharsets.UTF_8), keyBytes, ivBytes);

        byte[] combined = new byte[ivBytes.length + encryptedContent.length];
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(encryptedContent, 0, combined, ivBytes.length, encryptedContent.length);
        String encodedIvAndContent = Base64.getEncoder().encodeToString(combined);

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 4, 4, true)
                .toFormatter();
        String requestID = messageID + "-" + LocalDateTime.now().format(formatter);

        return String.format(
                "{\"requestID\": \"%s\", \"service\": \"SMSGW-PUSHSERV\", \"encryptedKey\": \"%s\", " +
                "\"oaepHashingAlgorithm\": \"NONE\", \"iv\": \"\", \"encryptedData\": \"%s\", " +
                "\"clientInfo\": \"\", \"optionalParam\": \"\"}",
                requestID, encodedEncryptedKey, encodedIvAndContent);
    }

    public String decryptPayload(String encryptedResponseJson) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode encryptedPayload = mapper.readTree(encryptedResponseJson);
        String encryptedKey = encryptedPayload.path("encryptedKey").asText();
        String encryptedData = encryptedPayload.path("encryptedData").asText();
        String iv = encryptedPayload.path("iv").asText();

        byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedKey);
        byte[] keyBytes = decryptRSA(encryptedKeyBytes);
        byte[] combinedIvAndContent = Base64.getDecoder().decode(encryptedData);
        byte[] ivBytes;
        byte[] encryptedContent;

        if (iv != null && !iv.trim().isEmpty()) {
            ivBytes = decodeIv(iv);
            encryptedContent = combinedIvAndContent;
        } else {
            if (combinedIvAndContent.length <= AES_BLOCK_SIZE) {
                throw new IllegalArgumentException("encryptedData does not contain IV and encrypted content");
            }
            ivBytes = Arrays.copyOfRange(combinedIvAndContent, 0, AES_BLOCK_SIZE);
            encryptedContent = Arrays.copyOfRange(combinedIvAndContent, AES_BLOCK_SIZE, combinedIvAndContent.length);
        }

        byte[] plainResponse = decryptAES(encryptedContent, keyBytes, ivBytes);
        return new String(plainResponse, StandardCharsets.UTF_8);
    }

    private byte[] encryptRSA(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PublicKey publicKey = load(publicKeyPath);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    private byte[] decryptRSA(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    private byte[] encryptAES(byte[] data, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(data);
    }

    private byte[] decryptAES(byte[] data, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(data);
    }

    private byte[] decodeIv(String iv) {
        try {
            byte[] decodedIv = Base64.getDecoder().decode(iv);
            if (decodedIv.length == AES_BLOCK_SIZE) {
                return decodedIv;
            }
        } catch (IllegalArgumentException ex) {
            log.debug("IV is not Base64 encoded, using raw UTF-8 bytes");
        }
        return iv.getBytes(StandardCharsets.UTF_8);
    }

    public PublicKey load(String cerFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(cerFilePath);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(fis);
        return certificate.getPublicKey();
    }

    private PrivateKey loadPrivateKey(String privateKeyPath) throws Exception {
        byte[] keyBytes = readPrivateKeyBytes(privateKeyPath);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private byte[] readPrivateKeyBytes(String privateKeyPath) throws IOException {
        String privateKey = new String(Files.readAllBytes(Paths.get(privateKeyPath)), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(privateKey);
    }
}
