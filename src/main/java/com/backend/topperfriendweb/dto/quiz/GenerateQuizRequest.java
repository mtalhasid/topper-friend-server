package com.backend.topperfriendweb.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateQuizRequest {
    @NotBlank(message = "Text is required")
    private String text;
    
    @NotNull(message = "Action is required")
    private String action = "summarize"; // Default to summarize
}
