package com.wizni;

public class Metadata {
    String timestamp;
    String level;
    String serviceName;
    String region;
    String zone;
    String userId;
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
    public String getZone() {
        return zone;
    }
    public void setZone(String zone) {
        this.zone = zone;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    @Override
    public String toString() {
	return "Metadata [timestamp=" + timestamp + ", level=" + level
		+ ", serviceName=" + serviceName + ", region=" + region
		+ ", zone=" + zone + ", userId=" + userId + "]";
    }
}
