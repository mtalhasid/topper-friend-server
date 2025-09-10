package com.backend.topperfriendweb.dto;

import java.time.LocalDateTime;

public class CollectionItemDTO {
    private Long id;
    private String itemType;
    private LocalDateTime addedAt;

    private NoteDTO note;          // only if itemType = NOTE
    private StudyPlanDTO studyPlan; // only if itemType = STUDY_PLAN

    // getters + setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    public NoteDTO getNote() { return note; }
    public void setNote(NoteDTO note) { this.note = note; }
    public StudyPlanDTO getStudyPlan() { return studyPlan; }
    public void setStudyPlan(StudyPlanDTO studyPlan) { this.studyPlan = studyPlan; }
}
