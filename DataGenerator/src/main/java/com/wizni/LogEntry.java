package com.wizni;

import com.wizni.AppInfo;
import com.wizni.HttpRequest;
import com.wizni.Metadata;

public class LogEntry {
    Metadata metadata = new Metadata();
    String log;
    String requestId;
    HttpRequest httpRequest = new HttpRequest();
    HttpResponse httpResponse = new HttpResponse();
    AppInfo appInfo =  new AppInfo();
    
    
    public Metadata getMetadata() {
        return metadata;
    }
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }
    public String getRequestId() {
        return requestId;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }
    
    public HttpResponse getHttpResponse() {
        return httpResponse;
    }
    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }
    public AppInfo getAppInfo() {
        return appInfo;
    }
    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;
    }
    @Override
    public String toString() {
	return "LogEntry [metadata=" + metadata + ", log=" + log
		+ ", requestId=" + requestId + ", httpRequest=" + httpRequest
		+ ", httpResponse=" + httpResponse + ", appInfo=" + appInfo
		+ "]";
    }
    
}
