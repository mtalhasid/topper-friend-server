package com.backend.topperfriendweb.dto;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.StudyPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}