package com.backend.topperfriendweb.dto;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.StudyPlanStatus;

import java.time.LocalDateTime;

public class StudyPlanDTO {
    private Long id;
    private String pdfUrl;
    private String pdfTitle;
    private String markdown;
    private String resources;
    private String tasks;
    private StudyPlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public StudyPlanStatus getStatus() {
        return status;
    }

    public void setStatus(StudyPlanStatus status) {
        this.status = status;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public String getPdfTitle() {
        return pdfTitle;
    }

    public void setPdfTitle(String pdfTitle) {
        this.pdfTitle = pdfTitle;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Constructor from Entity
    public StudyPlanDTO(StudyPlan studyPlan) {
        this.id = studyPlan.getId();
        this.pdfUrl = studyPlan.getPdfUrl();
        this.pdfTitle = studyPlan.getPdfTitle();
        this.markdown = studyPlan.getMarkdown();
        this.resources = studyPlan.getResources();
        this.tasks = studyPlan.getTasks();
        this.status = studyPlan.getStatus();
        this.createdAt = studyPlan.getCreatedAt();
        this.updatedAt = studyPlan.getUpdatedAt();
    }

    // Getters and setters
}