package com.icici.successmicroservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SuccessMessagePayload {

    @JsonProperty("msg_id")
    private String msgId;
    @JsonProperty("dept_id")
    private String deptId;
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("mobile_no")
    private String mobileNo;
    @JsonProperty("msg_text")
    private String msgText;
    @JsonProperty("pin")
    private String pin;
    @JsonProperty("pop_mail_id")
    private String popMailId;
    @JsonProperty("pop_sender_addr")
    private String popSenderAddr;
    @JsonProperty("tsg_details")
    private String tsgDetails;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("vendor_cd")
    private String vendorCd;
    @JsonProperty("send_count")
    private String sendCount;
    @JsonProperty("topic_name")
    private String topicName;
    @JsonProperty("ack_id")
    private String ackId;
    @JsonProperty("vendor_response")
    private String vendorResponse;
    @JsonProperty("dept_id_old")
    private String deptIdOld;
    @JsonProperty("app_id_old")
    private String appIdOld;

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public String getMsgText() { return msgText; }
    public void setMsgText(String msgText) { this.msgText = msgText; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public String getPopMailId() { return popMailId; }
    public void setPopMailId(String popMailId) { this.popMailId = popMailId; }
    public String getPopSenderAddr() { return popSenderAddr; }
    public void setPopSenderAddr(String popSenderAddr) { this.popSenderAddr = popSenderAddr; }
    public String getTsgDetails() { return tsgDetails; }
    public void setTsgDetails(String tsgDetails) { this.tsgDetails = tsgDetails; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getVendorCd() { return vendorCd; }
    public void setVendorCd(String vendorCd) { this.vendorCd = vendorCd; }
    public String getSendCount() { return sendCount; }
    public void setSendCount(String sendCount) { this.sendCount = sendCount; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
    public String getAckId() { return ackId; }
    public void setAckId(String ackId) { this.ackId = ackId; }
    public String getVendorResponse() { return vendorResponse; }
    public void setVendorResponse(String vendorResponse) { this.vendorResponse = vendorResponse; }
    public String getDeptIdOld() { return deptIdOld; }
    public void setDeptIdOld(String deptIdOld) { this.deptIdOld = deptIdOld; }
    public String getAppIdOld() { return appIdOld; }
    public void setAppIdOld(String appIdOld) { this.appIdOld = appIdOld; }
}
