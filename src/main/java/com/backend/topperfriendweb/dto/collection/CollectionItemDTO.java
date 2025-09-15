package com.backend.topperfriendweb.dto.collection;

import java.time.LocalDateTime;

import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItemDTO {
    private Long id;
    private String itemType;
    private LocalDateTime addedAt;

    private NoteDTO note;          // only if itemType = NOTE
    private StudyPlanDTO studyPlan; // only if itemType = STUDY_PLAN
}
