package com.backend.topperfriendweb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResponse {
    private boolean success;
    private int score;
    private int totalQuestions;
    private String weaknessSummary;
    private String message;
}
