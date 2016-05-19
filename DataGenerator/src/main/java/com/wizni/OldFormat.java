package com.wizni;

public class OldFormat {
    String date;
    String logLevel;
    String appId;
    String userId;
    String httpMethod;
    String reqId;
    String request;
    String response;
    int statusCode;
    String statusMessage;
    int responseTime;
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getLogLevel() {
        return logLevel;
    }
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getHttpMethod() {
        return httpMethod;
    }
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    public String getReqId() {
        return reqId;
    }
    public void setReqId(String reqId) {
        this.reqId = reqId;
    }
    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public String getStatusMessage() {
        return statusMessage;
    }
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    public int getResponseTime() {
        return responseTime;
    }
    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }
    @Override
    public String toString() {
	return "OldFormat [date=" + date + ", logLevel=" + logLevel
		+ ", appId=" + appId + ", userId=" + userId + ", httpMethod="
		+ httpMethod + ", reqId=" + reqId + ", request=" + request
		+ ", response=" + response + ", statusCode=" + statusCode
		+ ", statusMessage=" + statusMessage + ", responseTime="
		+ responseTime + "]";
    }

}
