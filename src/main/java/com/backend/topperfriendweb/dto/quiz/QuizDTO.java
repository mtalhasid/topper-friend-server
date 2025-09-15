package com.backend.topperfriendweb.dto.quiz;

import com.backend.topperfriendweb.model.Quiz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private Long id;
    private String title;
    private String questionsJson;
    private String weaknessSummary;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
    private Long userId;

    // Constructor from Entity
    public QuizDTO(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.questionsJson = quiz.getQuestionsJson();
        this.weaknessSummary = quiz.getWeaknessSummary();
        this.totalQuestions = quiz.getTotalQuestions();
        this.createdAt = quiz.getCreatedAt();
        this.userId = quiz.getUser() != null ? quiz.getUser().getId() : null;
    }
}
