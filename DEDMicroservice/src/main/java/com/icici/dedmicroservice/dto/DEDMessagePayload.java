package com.icici.dedmicroservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DEDMessagePayload {

    @JsonProperty("msg_id")
    private String msgId;
    @JsonProperty("dept_id")
    private String deptId;
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("mobile_no")
    private String mobileNo;
    @JsonProperty("err_desc")
    private String errDesc;
    @JsonProperty("error_source")
    private String errorSource;
    @JsonProperty("err_dttime")
    private String errDttime;
    @JsonProperty("err_stack_trace")
    private String errStackTrace;
    @JsonProperty("err_vendor_code")
    private String errVendorCode;
    @JsonProperty("request_param")
    private String requestParam;
    @JsonProperty("tsg_details")
    private String tsgDetails;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("vendor_cd")
    private String vendorCd;
    @JsonProperty("topic_name")
    private String topicName;

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public String getErrDesc() { return errDesc; }
    public void setErrDesc(String errDesc) { this.errDesc = errDesc; }
    public String getErrorSource() { return errorSource; }
    public void setErrorSource(String errorSource) { this.errorSource = errorSource; }
    public String getErrDttime() { return errDttime; }
    public void setErrDttime(String errDttime) { this.errDttime = errDttime; }
    public String getErrStackTrace() { return errStackTrace; }
    public void setErrStackTrace(String errStackTrace) { this.errStackTrace = errStackTrace; }
    public String getErrVendorCode() { return errVendorCode; }
    public void setErrVendorCode(String errVendorCode) { this.errVendorCode = errVendorCode; }
    public String getRequestParam() { return requestParam; }
    public void setRequestParam(String requestParam) { this.requestParam = requestParam; }
    public String getTsgDetails() { return tsgDetails; }
    public void setTsgDetails(String tsgDetails) { this.tsgDetails = tsgDetails; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getVendorCd() { return vendorCd; }
    public void setVendorCd(String vendorCd) { this.vendorCd = vendorCd; }
    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }
}
