package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.*;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.GeminiService;
import com.backend.topperfriendweb.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz-generator")
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
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<?> generateQuiz(@Valid @RequestBody GenerateQuizRequest request) {
        try {
            User user = getLoggedInUser();

            if ("summarize".equals(request.getAction())) {
                String summary = geminiService.summarize(request.getText());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "result", summary,
                        "type", "summary"
                ));
            } else if ("generate-quiz".equals(request.getAction())) {
                String jsonResponse = geminiService.generateQuizJson(request.getText());
                QuizDTO quiz = quizService.createQuiz("Generated Quiz from Topic", jsonResponse, user);
                
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "quizzes", List.of(quiz),
                        "type", "quiz"
                ));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Invalid action"));
        } catch (Exception e) {
            log.error("Error generating quiz", e);
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserQuizzes() {
        try {
            User user = getLoggedInUser();
            List<QuizDTO> quizzes = quizService.getUserQuizzes(user.getId());
            return ResponseEntity.ok(Map.of("success", true, "quizzes", quizzes));
        } catch (Exception e) {
            log.error("Error getting user quizzes", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuizById(@PathVariable Long quizId) {
        try {
            QuizDTO quiz = quizService.getQuizById(quizId);
            return ResponseEntity.ok(Map.of("success", true, "quiz", quiz));
        } catch (Exception e) {
            log.error("Error getting quiz by ID", e);
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@Valid @RequestBody SubmitQuizRequest request) {
        try {
            User user = getLoggedInUser();
            QuizSubmissionResponse response = quizService.submitQuiz(request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error submitting quiz", e);
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long quizId) {
        try {
            User user = getLoggedInUser();
            quizService.deleteQuiz(quizId, user.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Quiz deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting quiz", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}