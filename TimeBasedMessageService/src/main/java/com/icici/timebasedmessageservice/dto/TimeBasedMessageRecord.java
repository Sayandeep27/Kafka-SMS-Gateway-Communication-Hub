package com.icici.timebasedmessageservice.dto;

import java.time.LocalDateTime;

public class TimeBasedMessageRecord {

    private String msgId;
    private String deptId;
    private String appId;
    private String appCode;
    private String partnerCode;
    private String pin;
    private String popMailId;
    private String popSenderAddr;
    private String deptMsgId;
    private String mobileNo;
    private String msgText;
    private LocalDateTime msgSendFromDttime;
    private LocalDateTime msgSendToDttime;
    private String noDelvryFromTime;
    private String noDelvryToTime;
    private String msgHttpMode;
    private String msgStts;
    private String rqstAckId;
    private LocalDateTime rqstAckDttime;
    private String info1;
    private String info2;
    private String info3;
    private String info4;
    private String sendCount;
    private String pipeId;
    private String deptIdOld;
    private String appIdOld;
    private String prfrrdChannel;
    private String altChannel;
    private LocalDateTime altChannelSendFromDttime;
    private LocalDateTime altChannelSendToDttime;
    private LocalDateTime msgSourceTimestamp;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPopMailId() {
        return popMailId;
    }

    public void setPopMailId(String popMailId) {
        this.popMailId = popMailId;
    }

    public String getPopSenderAddr() {
        return popSenderAddr;
    }

    public void setPopSenderAddr(String popSenderAddr) {
        this.popSenderAddr = popSenderAddr;
    }

    public String getDeptMsgId() {
        return deptMsgId;
    }

    public void setDeptMsgId(String deptMsgId) {
        this.deptMsgId = deptMsgId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public LocalDateTime getMsgSendFromDttime() {
        return msgSendFromDttime;
    }

    public void setMsgSendFromDttime(LocalDateTime msgSendFromDttime) {
        this.msgSendFromDttime = msgSendFromDttime;
    }

    public LocalDateTime getMsgSendToDttime() {
        return msgSendToDttime;
    }

    public void setMsgSendToDttime(LocalDateTime msgSendToDttime) {
        this.msgSendToDttime = msgSendToDttime;
    }

    public String getNoDelvryFromTime() {
        return noDelvryFromTime;
    }

    public void setNoDelvryFromTime(String noDelvryFromTime) {
        this.noDelvryFromTime = noDelvryFromTime;
    }

    public String getNoDelvryToTime() {
        return noDelvryToTime;
    }

    public void setNoDelvryToTime(String noDelvryToTime) {
        this.noDelvryToTime = noDelvryToTime;
    }

    public String getMsgHttpMode() {
        return msgHttpMode;
    }

    public void setMsgHttpMode(String msgHttpMode) {
        this.msgHttpMode = msgHttpMode;
    }

    public String getMsgStts() {
        return msgStts;
    }

    public void setMsgStts(String msgStts) {
        this.msgStts = msgStts;
    }

    public String getRqstAckId() {
        return rqstAckId;
    }

    public void setRqstAckId(String rqstAckId) {
        this.rqstAckId = rqstAckId;
    }

    public LocalDateTime getRqstAckDttime() {
        return rqstAckDttime;
    }

    public void setRqstAckDttime(LocalDateTime rqstAckDttime) {
        this.rqstAckDttime = rqstAckDttime;
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getInfo2() {
        return info2;
    }

    public void setInfo2(String info2) {
        this.info2 = info2;
    }

    public String getInfo3() {
        return info3;
    }

    public void setInfo3(String info3) {
        this.info3 = info3;
    }

    public String getInfo4() {
        return info4;
    }

    public void setInfo4(String info4) {
        this.info4 = info4;
    }

    public String getSendCount() {
        return sendCount;
    }

    public void setSendCount(String sendCount) {
        this.sendCount = sendCount;
    }

    public String getPipeId() {
        return pipeId;
    }

    public void setPipeId(String pipeId) {
        this.pipeId = pipeId;
    }

    public String getDeptIdOld() {
        return deptIdOld;
    }

    public void setDeptIdOld(String deptIdOld) {
        this.deptIdOld = deptIdOld;
    }

    public String getAppIdOld() {
        return appIdOld;
    }

    public void setAppIdOld(String appIdOld) {
        this.appIdOld = appIdOld;
    }

    public String getPrfrrdChannel() {
        return prfrrdChannel;
    }

    public void setPrfrrdChannel(String prfrrdChannel) {
        this.prfrrdChannel = prfrrdChannel;
    }

    public String getAltChannel() {
        return altChannel;
    }

    public void setAltChannel(String altChannel) {
        this.altChannel = altChannel;
    }

    public LocalDateTime getAltChannelSendFromDttime() {
        return altChannelSendFromDttime;
    }

    public void setAltChannelSendFromDttime(LocalDateTime altChannelSendFromDttime) {
        this.altChannelSendFromDttime = altChannelSendFromDttime;
    }

    public LocalDateTime getAltChannelSendToDttime() {
        return altChannelSendToDttime;
    }

    public void setAltChannelSendToDttime(LocalDateTime altChannelSendToDttime) {
        this.altChannelSendToDttime = altChannelSendToDttime;
    }

    public LocalDateTime getMsgSourceTimestamp() {
        return msgSourceTimestamp;
    }

    public void setMsgSourceTimestamp(LocalDateTime msgSourceTimestamp) {
        this.msgSourceTimestamp = msgSourceTimestamp;
    }
}
