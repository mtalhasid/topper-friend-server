package com.backend.topperfriendweb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitQuizRequest {
    @NotNull(message = "Quiz ID is required")
    private Long quizId;
    
    @NotNull(message = "Answers are required")
    private List<Integer> answers;
}
