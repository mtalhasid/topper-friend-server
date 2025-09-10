package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.GeminiService;
import com.backend.topperfriendweb.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz-generator")
public class QuizController {
    private final GeminiService geminiService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public QuizController(GeminiService geminiService, JwtUtil jwtUtil,
                          UserRepository userRepository, QuizRepository quizRepository) {
        this.geminiService = geminiService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
    }

    @PostMapping
    public ResponseEntity<?> generateQuiz(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String text = (String) body.get("text");
            String action = (String) body.getOrDefault("action", "summarize");

            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
            }

            if ("summarize".equals(action)) {
                try {
                    String summary = geminiService.summarize(text);
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "result", summary,
                            "type", "summary"));
                } catch (Exception e) {
                    return ResponseEntity.status(503).body(Map.of(
                            "error", "AI service temporarily unavailable: " + e.getMessage()));
                }
            } else if ("generate-quiz".equals(action)) {
                try {
                    List<Quiz> quizzes = geminiService.generateQuiz(text, user);
                    quizRepository.saveAll(quizzes);

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "quizzes", quizzes,
                            "type", "quiz"));
                } catch (Exception e) {
                    return ResponseEntity.status(503).body(Map.of(
                            "error", "Failed to generate quiz: " + e.getMessage()));
                }
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Invalid action"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserQuizzes(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Quiz> quizzes = quizRepository.findByUserId(user.getId());
            return ResponseEntity.ok(Map.of("success", true, "quizzes", quizzes));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {

        try {
            // FIX: Better handling of quizId conversion
            Long quizId;
            Object quizIdObj = body.get("quizId");
            if (quizIdObj instanceof Integer) {
                quizId = ((Integer) quizIdObj).longValue();
            } else if (quizIdObj instanceof Long) {
                quizId = (Long) quizIdObj;
            } else if (quizIdObj instanceof String) {
                quizId = Long.valueOf((String) quizIdObj);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid quizId format"));
            }

            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));

            try {
                String aiText = quiz.getQuestionsJson();
                String weaknessSummary = geminiService.analyzeWeakness(aiText);

                quiz.setWeaknessSummary(weaknessSummary);
                quizRepository.save(quiz);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "weaknessSummary", weaknessSummary));
            } catch (Exception e) {
                return ResponseEntity.status(503).body(Map.of(
                        "error", "AI analysis service temporarily unavailable: " + e.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}