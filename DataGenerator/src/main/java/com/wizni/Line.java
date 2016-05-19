package com.wizni;

import com.wizni.SourceLocation;

public class Line {
    String time;
    String level;
    String logMessage;
    SourceLocation sourceLocation;
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
    public String getLogMessage() {
        return logMessage;
    }
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }
    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }
    @Override
    public String toString() {
	return "Line [time=" + time + ", level=" + level + ", logMessage="
		+ logMessage + ", sourceLocation=" + sourceLocation + "]";
    }
}
