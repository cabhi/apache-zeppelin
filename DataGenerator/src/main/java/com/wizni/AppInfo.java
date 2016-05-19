package com.wizni;

import java.util.Arrays;

import com.wizni.Line;
import com.wizni.SourceReference;

public class AppInfo {
    String appId;
    String moduleId;   
    String versionId;
    String ip;
    String startTime;   
    String endTime;
    String method;   
    String resource;
    String error;
    String httpVersion;
    String nickname;
    String urlMapEntry;
    String instanceId;
    Line[] lines;
    SourceReference[] sourceReferences;
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public String getModuleId() {
        return moduleId;
    }
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    public String getVersionId() {
        return versionId;
    }
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getResource() {
        return resource;
    }
    public void setResource(String resource) {
        this.resource = resource;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public String getHttpVersion() {
        return httpVersion;
    }
    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getUrlMapEntry() {
        return urlMapEntry;
    }
    public void setUrlMapEntry(String urlMapEntry) {
        this.urlMapEntry = urlMapEntry;
    }
    public String getInstanceId() {
        return instanceId;
    }
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    public Line[] getLines() {
        return lines;
    }
    public void setLines(Line[] lines) {
        this.lines = lines;
    }
    public SourceReference[] getSourceReferences() {
        return sourceReferences;
    }
    public void setSourceReferences(SourceReference[] sourceReferences) {
        this.sourceReferences = sourceReferences;
    }
    @Override
    public String toString() {
	return "AppInfo [appId=" + appId + ", moduleId=" + moduleId
		+ ", versionId=" + versionId + ", ip=" + ip + ", startTime="
		+ startTime + ", endTime=" + endTime + ", method=" + method
		+ ", resource=" + resource + ", error=" + error
		+ ", httpVersion=" + httpVersion + ", nickname=" + nickname
		+ ", urlMapEntry=" + urlMapEntry + ", instanceId=" + instanceId
		+ ", lines=" + Arrays.toString(lines) + ", sourceReferences="
		+ Arrays.toString(sourceReferences) + "]";
    }
}
