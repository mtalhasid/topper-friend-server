package com.backend.topperfriendweb.dto.studyplan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStudyPlanPdfRequest {
    @Size(max = 2000, message = "pdfUrl too long")
    private String pdfUrl; // nullable to clear

    @NotNull(message = "pdfTitle cannot be null")
    @Size(max = 255, message = "pdfTitle too long")
    private String pdfTitle;
}
