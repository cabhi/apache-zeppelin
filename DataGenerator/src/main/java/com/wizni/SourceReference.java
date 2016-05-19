package com.wizni;

public class SourceReference {
    String repository;
    String revisionId;
    
    public SourceReference(String repository, String revisionId) {
	this.repository = repository;
	this.revisionId = revisionId;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    @Override
    public String toString() {
	return "SourceReference [repository=" + repository + ", revisionId="
		+ revisionId + "]";
    }
}
