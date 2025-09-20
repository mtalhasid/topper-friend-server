package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.quiz.*;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.quiz.GeminiService;
import com.backend.topperfriendweb.service.quiz.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-generator")
@PreAuthorize("isAuthenticated()")
@Validated
@RequiredArgsConstructor
@Slf4j
public class QuizController {
    private final QuizService quizService;
    private final GeminiService geminiService;
    private final UserRepository userRepository;

    // Helper to get logged-in user
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<QuizGenerationResponse>> generateQuiz(
            @Valid @RequestBody GenerateQuizRequest request) {
        User user = getLoggedInUser();

        if ("summarize".equals(request.getAction())) {
            String summary = geminiService.summarize(request.getText());
            QuizGenerationResponse response = new QuizGenerationResponse(
                    summary,
                    null,
                    "summary"
            );
            return ResponseEntity.ok(CommonResponse.success("Text summarized successfully", response));

        } else if ("generate-quiz".equals(request.getAction())) {
            String jsonResponse = geminiService.generateQuizJson(request.getText());
            QuizDTO quiz = quizService.createQuiz("Generated Quiz from Topic", jsonResponse, user);

            QuizGenerationResponse response = new QuizGenerationResponse(
                    null,
                    List.of(quiz),
                    "quiz"
            );
            return ResponseEntity.ok(CommonResponse.success("Quiz generated successfully", response));
        }

        throw new IllegalArgumentException("Invalid action. Must be 'summarize' or 'generate-quiz'");
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<QuizDTO>>> getUserQuizzes() {
        User user = getLoggedInUser();
        List<QuizDTO> quizzes = quizService.getUserQuizzes(user.getId());
        return ResponseEntity.ok(CommonResponse.success("User quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<CommonResponse<QuizDTO>> getQuizById(@PathVariable Long quizId) {
        QuizDTO quiz = quizService.getQuizById(quizId);
        return ResponseEntity.ok(CommonResponse.success("Quiz retrieved successfully", quiz));
    }

    @PostMapping("/submit")
    public ResponseEntity<CommonResponse<QuizSubmissionResponse>> submitQuiz(
            @Valid @RequestBody SubmitQuizRequest request) {
        User user = getLoggedInUser();
        QuizSubmissionResponse response = quizService.submitQuiz(request, user);
        return ResponseEntity.ok(CommonResponse.success("Quiz submitted successfully", response));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<CommonResponse<String>> deleteQuiz(@PathVariable Long quizId) {
        User user = getLoggedInUser();
        quizService.deleteQuiz(quizId, user.getId());
        return ResponseEntity.ok(CommonResponse.success("Quiz deleted successfully"));
    }
}