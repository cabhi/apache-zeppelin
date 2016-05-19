package com.wizni;

public class SourceLocation {
    String file;
    String line;
//    String functionName;
    
    public SourceLocation(String file, String line) {
	this.file = file;
	this.line = line;
//	this.functionName = functionName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

//    public String getFunctionName() {
//        return functionName;
//    }
//
//    public void setFunctionName(String functionName) {
//        this.functionName = functionName;
//    }

    @Override
    public String toString() {
	return "SourceLocation [file=" + file + ", line=" + line + "]";
    }
    
//    @Override
//    public String toString() {
//	return "SourceLocation [file=" + file + ", line=" + line
//		+ ", functionName=" + functionName + "]";
//    }
}
