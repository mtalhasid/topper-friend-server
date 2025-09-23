package com.backend.topperfriendweb.dto.studyplan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStudyPlanResourcesRequest {
    @NotNull(message = "resources cannot be null")
    @Size(max = 200000, message = "resources too long")
    private String resources;
}
