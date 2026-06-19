package com.icici.vendorhealthcheckservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GatewayRequestBody {
    private String mobile_no;
    private String sender_id;
    private String message;
    private String msgid;
    private String deptid;
    private String appid;

    @JsonProperty("sms_TYPE")
    private String sms_TYPE;

    private String vendor_cd;
    private String channel;

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getDeptid() {
        return deptid;
    }

    public void setDeptid(String deptid) {
        this.deptid = deptid;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSms_TYPE() {
        return sms_TYPE;
    }

    public void setSms_TYPE(String sms_TYPE) {
        this.sms_TYPE = sms_TYPE;
    }

    public String getVendor_cd() {
        return vendor_cd;
    }

    public void setVendor_cd(String vendor_cd) {
        this.vendor_cd = vendor_cd;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
