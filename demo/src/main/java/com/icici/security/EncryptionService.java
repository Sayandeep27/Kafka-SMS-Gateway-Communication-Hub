package com.icici.security;
 
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
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
 
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
@Service
public class EncryptionService {
    
    private static Logger log = LogManager.getLogger();
    private static final int AES_BLOCK_SIZE = 16;
    
    @Value("${crypto.public-key-path:D:\\key\\ICICI_UAT_PublicCert.cer}")
    private String publicKeyPath;
    
    @Value("${crypto.private-key-path:D:\\key\\DISPENSER_PrivateKey.pem}")
    private String privateKeyPath;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
     
    public String encryptPayload(String data, String messageID) throws Exception {
        String requestID="";
        String encodedEncryptedKey="",encodedIvAndContent="";
        
        log.info("publicKeyPath==="+publicKeyPath);
        try
        {
            log.info("Inside the encryptPayload");
        // 1. Generate random 16-digit skey
//        String skey =UUID.randomUUID().toString();
        String skey =   RandomStringUtils.randomAlphanumeric(16);
//          String skey="";
        log.info("1111111111=="+skey);
        byte[] keyBytes = skey.getBytes(StandardCharsets.UTF_8);
        log.info("1111111111=="+skey);
        
        
        log.info("133333333333333333333333333333=");
        // 2. Encrypt skey using RSA (Digital Envelope)
        // Matches: RSA/ECB/PKCS1Padding
        byte[] encryptedKeyBytes = encryptRSA(keyBytes);
        log.info("encodedEncryptedKey2W222EEEEEEEEE==");
        encodedEncryptedKey = Base64.getEncoder().encodeToString(encryptedKeyBytes);
        log.info("encodedEncryptedKey=="+encodedEncryptedKey);
        // 3. Generate random 16-digit IV
//        String sIV = RandomStringUtils.randomAlphanumeric(16);
        String sIV=RandomStringUtils.randomAlphanumeric(16);
        byte[] ivBytes = sIV.getBytes(StandardCharsets.UTF_8);
         log.info("sIV="+sIV);
        // 4. Encrypt content using AES/CBC/PKCS5Padding
        byte[] encryptedContent = encryptAES(data.getBytes(StandardCharsets.UTF_8), keyBytes, ivBytes);
        log.info("22222222222222");
        // 5. Merge IV and Encrypted Content (matches helper.MergeTwoByteArrays)
        byte[] combined = new byte[ivBytes.length + encryptedContent.length];
        log.info("2223333333");
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        log.info("444444444444");
        System.arraycopy(encryptedContent, 0, combined, ivBytes.length, encryptedContent.length);
        log.info("5555555555555555");
        encodedIvAndContent = Base64.getEncoder().encodeToString(combined);
        log.info("666666666666");
        // 6. Construct JSON Response (Matching your requestID logic)
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 4, 4, true) // min 0, max 4 digits
                .toFormatter();
        requestID = messageID+"-"+LocalDateTime.now().format(formatter);
        log.info("77777777777");
        }catch(Exception ex)
        {
            log.error("Error occurred at encryptPayload",ex);
//           System.out.println("Error occurred at encryptPayload "+ex);
        }
 
        return String.format(
            "{\"requestID\": \"%s\", \"service\": \"SMSGW-PUSHSERV\", \"encryptedKey\": \"%s\", " +
            "\"oaepHashingAlgorithm\": \"NONE\", \"iv\": \"\", \"encryptedData\": \"%s\", " +
            "\"clientInfo\": \"\", \"optionalParam\": \"\"}",
            requestID, encodedEncryptedKey, encodedIvAndContent
        );
    }
 
    private byte[] encryptRSA(byte[] data) throws Exception {
        
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        
        try {
//          PublicKey publicKey = loadPublicKey("D:\\ key\\ICICI_UAT_PublicCert.pem");
            PublicKey publicKey =load(publicKeyPath);
            log.info("publicKeyPath-------------"+publicKeyPath);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        }
        catch(Exception ex)
        {
            log.error("Error occurred at encryptRSA",ex);
//           System.out.println("Error occurred at encryptRSA "+ex);
        }
        return cipher.doFinal(data);
    }
    
    public String decryptPayload(String encryptedResponseJson) throws Exception {
        log.info("Inside decryptPayload");
        JsonNode encryptedPayload = objectMapper.readTree(encryptedResponseJson);
        String encryptedKey = encryptedPayload.path("encryptedKey").asText();
        String encryptedData = encryptedPayload.path("encryptedData").asText();
        String iv = encryptedPayload.path("iv").asText();
        
        if (encryptedKey == null || encryptedKey.trim().isEmpty()) {
            throw new IllegalArgumentException("encryptedKey is missing in encrypted response");
        }
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            throw new IllegalArgumentException("encryptedData is missing in encrypted response");
        }
        
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
 
    private PublicKey loadPublicKey(String path) throws Exception {
        X509EncodedKeySpec spec = null;
        try {
            log.info("insode the loadPublicKey ");
        String key = new String(Files.readAllBytes(Paths.get(path)))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        log.info("insode the loadPublicKey11111111111=="+key);
        key=key.replace("-----BEGINCERTIFICATE-----", "").replace("-----ENDCERTIFICATE-----", "");
        log.info("Key=="+key);
       
        System.out.println("insode the loadPublicKey 222222222222");
//        spec = new PKCS8EncodedKeySpec(decode);
        byte[] decode = Base64.getDecoder().decode(key);
        spec = new X509EncodedKeySpec(decode);
        KeyFactory.getInstance("RSA").generatePublic(spec);
        log.info("insode the loadPublicKey 333333333333333 ");
    }
    catch(Exception ex)
    {
        log.error("Error occurred at loadPublicKey",ex);
//       System.out.println("Error occurred at loadPublicKey "+ex);
    }
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
    
    public PublicKey load(String cerFilePath) throws Exception
    {
        FileInputStream fis=new FileInputStream(cerFilePath);
        CertificateFactory cf=CertificateFactory.getInstance("X.509");
        X509Certificate certificate=(X509Certificate) cf.generateCertificate(fis);
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
        
        if (privateKey.contains("BEGINRSAPRIVATEKEY")) {
            throw new IOException("PKCS#1 RSA private key is not supported directly. Use PKCS#8 private key format.");
        }
        
        return Base64.getDecoder().decode(privateKey);
    }
    
//  private String publicKeyPath;
//  
//  public String encryptPayload(String data,String messageId ) throws Exception
//  {
//      String sKey=RandomStringUtils.randomAlphanumeric(16);
//      byte[] keyBytes=sKey.getBytes(StandardCharsets.UTF_8);
//      
//      byte[] encrytedKeyBytes=encryptRSA(keyBytes);
//      
//      String encodedEncrytedKey=java.util.Base64.getEncoder().encodeToString(encrytedKeyBytes);
//      
//      String sIV=RandomStringUtils.randomAlphanumeric(16);
//      byte[] ivBytes=sIV.getBytes(StandardCharsets.UTF_8);
//      
//      byte[] encrytedContent=encryptAES(data.getBytes(StandardCharsets.UTF_8),keyBytes,ivBytes);
//      
//      byte[] combined=new byte[ivBytes.length+encrytedContent.length];
//      
//      System.arraycopy(encrytedContent, 0, combined, ivBytes.length, encrytedContent.length);
//      
//      String encodedIvAndContent=java.util.Base64.getEncoder().encodeToString(combined);
//      
//      String requestId="";
//      
//      
//      return "";
//  }
    
    
 
}
 
 
