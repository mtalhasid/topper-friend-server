package com.backend.topperfriendweb.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizGenerationResponse {
    private String result;           // For summary text
    private List<QuizDTO> quizzes;   // For generated quizzes
    private String type;             // "summary" or "quiz"

    // Constructor for summary response
    public QuizGenerationResponse(String summary) {
        this.result = summary;
        this.type = "summary";
        this.quizzes = null;
    }

    // Constructor for quiz response
    public QuizGenerationResponse(List<QuizDTO> quizzes) {
        this.quizzes = quizzes;
        this.type = "quiz";
        this.result = null;
    }
}