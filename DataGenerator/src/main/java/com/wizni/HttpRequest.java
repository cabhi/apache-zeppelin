package com.wizni;

public class HttpRequest {
    String requestMethod;
    String requestUrl;
    int requestSize;
    String request;
    String userAgent;
    String remoteIp;
    public String getRequestMethod() {
        return requestMethod;
    }
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    public String getRequestUrl() {
        return requestUrl;
    }
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    public int getRequestSize() {
        return requestSize;
    }
    public void setRequestSize(int requestSize) {
        this.requestSize = requestSize;
    }
    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getRemoteIp() {
        return remoteIp;
    }
    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }
    @Override
    public String toString() {
	return "HttpRequest [requestMethod=" + requestMethod + ", requestUrl="
		+ requestUrl + ", requestSize=" + requestSize + ", request="
		+ request + ", userAgent=" + userAgent + ", remoteIp="
		+ remoteIp + "]";
    }
}
