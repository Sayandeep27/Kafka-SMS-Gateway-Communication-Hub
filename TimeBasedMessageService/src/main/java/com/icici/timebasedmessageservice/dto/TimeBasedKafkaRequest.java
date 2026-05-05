package com.icici.timebasedmessageservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeBasedKafkaRequest {

    @JsonProperty("msg_id")
    private String msgId;
    @JsonProperty("dept")
    private String dept;
    @JsonProperty("appid")
    private String appId;
    @JsonProperty("mobile")
    private String mobile;
    @JsonProperty("deptmsgid")
    private String deptMsgId;
    @JsonProperty("message")
    private String message;
    @JsonProperty("fromdatetime")
    private String fromDatetime;
    @JsonProperty("todatetime")
    private String toDatetime;
    @JsonProperty("nodeliverytimefrom")
    private String noDeliveryTimeFrom;
    @JsonProperty("nodeliverytimeto")
    private String noDeliveryTimeTo;
    @JsonProperty("httpmode")
    private String httpMode;
    @JsonProperty("remarks")
    private String remarks;
    @JsonProperty("trn__generate_timestamp")
    private String trnGenerateTimestamp;
    @JsonProperty("duplicate_check")
    private String duplicateCheck;
    @JsonProperty("remarks1")
    private String remarks1;
    @JsonProperty("remarks2")
    private String remarks2;
    @JsonProperty("topic_name")
    private String topicName;
    @JsonProperty("dept_id_old")
    private String deptIdOld;
    @JsonProperty("app_id_old")
    private String appIdOld;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDeptMsgId() {
        return deptMsgId;
    }

    public void setDeptMsgId(String deptMsgId) {
        this.deptMsgId = deptMsgId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromDatetime() {
        return fromDatetime;
    }

    public void setFromDatetime(String fromDatetime) {
        this.fromDatetime = fromDatetime;
    }

    public String getToDatetime() {
        return toDatetime;
    }

    public void setToDatetime(String toDatetime) {
        this.toDatetime = toDatetime;
    }

    public String getNoDeliveryTimeFrom() {
        return noDeliveryTimeFrom;
    }

    public void setNoDeliveryTimeFrom(String noDeliveryTimeFrom) {
        this.noDeliveryTimeFrom = noDeliveryTimeFrom;
    }

    public String getNoDeliveryTimeTo() {
        return noDeliveryTimeTo;
    }

    public void setNoDeliveryTimeTo(String noDeliveryTimeTo) {
        this.noDeliveryTimeTo = noDeliveryTimeTo;
    }

    public String getHttpMode() {
        return httpMode;
    }

    public void setHttpMode(String httpMode) {
        this.httpMode = httpMode;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTrnGenerateTimestamp() {
        return trnGenerateTimestamp;
    }

    public void setTrnGenerateTimestamp(String trnGenerateTimestamp) {
        this.trnGenerateTimestamp = trnGenerateTimestamp;
    }

    public String getDuplicateCheck() {
        return duplicateCheck;
    }

    public void setDuplicateCheck(String duplicateCheck) {
        this.duplicateCheck = duplicateCheck;
    }

    public String getRemarks1() {
        return remarks1;
    }

    public void setRemarks1(String remarks1) {
        this.remarks1 = remarks1;
    }

    public String getRemarks2() {
        return remarks2;
    }

    public void setRemarks2(String remarks2) {
        this.remarks2 = remarks2;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
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
}
