package com.backend.topperfriendweb.dto.studyplan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStudyPlanMarkdownRequest {
    @NotNull(message = "markdown cannot be null")
    @Size(max = 200000, message = "markdown too long")
    private String markdown;
}
