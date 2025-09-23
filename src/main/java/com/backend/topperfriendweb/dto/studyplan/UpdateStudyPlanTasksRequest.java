package com.backend.topperfriendweb.dto.studyplan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStudyPlanTasksRequest {
    @NotNull(message = "tasks cannot be null")
    @Size(max = 200000, message = "tasks too long")
    private String tasks;
}
