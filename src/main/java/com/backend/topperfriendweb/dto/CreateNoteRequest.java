// src/main/java/com/backend/topperfriendweb/dto/CreateNoteRequest.java
package com.backend.topperfriendweb.dto;

import java.util.List;

public class CreateNoteRequest {
    private Long userId;
    private String title;
    private String pdfLink;
    private List<String> tags;
    private String pdfOption;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getPdfOption() {
        return pdfOption;
    }

    public void setPdfOption(String pdfOption) {
        this.pdfOption = pdfOption;
    }
}