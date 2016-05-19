package com.wizni;

public class HttpResponse {
    int status;
    String response;
    int responseSize;
    int responseTime;
    
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    public int getResponseSize() {
        return responseSize;
    }
    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }
    public int getResponseTime() {
        return responseTime;
    }
    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }
    @Override
    public String toString() {
	return "HttpResponse [status=" + status + ", response=" + response
		+ ", responseSize=" + responseSize + ", responseTime="
		+ responseTime + "]";
    }
    
    
}
