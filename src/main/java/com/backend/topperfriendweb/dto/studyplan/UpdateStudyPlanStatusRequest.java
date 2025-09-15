package com.backend.topperfriendweb.dto.studyplan;

import com.backend.topperfriendweb.model.StudyPlanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStudyPlanStatusRequest {
    @NotNull(message = "Status is required")
    private StudyPlanStatus status;
}
