package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.GeminiService;
import com.backend.topperfriendweb.utils.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
                    // Call Gemini service to generate quiz JSON
                    String jsonResponse = geminiService.generateQuizJson(text);

                    // Parse JSON array using Jackson
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode quizArray;
                    try {
                        quizArray = objectMapper.readTree(jsonResponse);
                        if (!quizArray.isArray()) {
                            throw new RuntimeException("Gemini did not return a JSON array");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Gemini returned invalid JSON: " + jsonResponse);
                    }

                    // Create Quiz entity
                    Quiz quiz = new Quiz();
                    quiz.setUser(user);
                    quiz.setTitle("Generated Quiz from Topic");
                    quiz.setQuestionsJson(jsonResponse); // store JSON string directly
                    quiz.setTotalQuestions(quizArray.size());

                    // Save Quiz
                    quizRepository.save(quiz);

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "quizzes", List.of(quiz),
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
            // 1️⃣ Get user
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2️⃣ Get quiz
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

            // 3️⃣ Get user answers from request
            List<Integer> userAnswers;
            try {
                userAnswers = (List<Integer>) body.get("answers");
                if (userAnswers == null) throw new RuntimeException("answers field is required");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid answers format"));
            }

            // 4️⃣ Parse questions JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode questionsArray;
            try {
                questionsArray = objectMapper.readTree(quiz.getQuestionsJson());
                if (!questionsArray.isArray()) {
                    throw new RuntimeException("quiz questionsJson is not an array");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse quiz questions JSON: " + e.getMessage());
            }

            // 5️⃣ Compute score
            int score = 0;
            int totalQuestions = questionsArray.size();
            for (int i = 0; i < totalQuestions; i++) {
                JsonNode questionNode = questionsArray.get(i);
                int correctAnswer = questionNode.get("correctAnswer").asInt();
                if (i < userAnswers.size() && userAnswers.get(i) == correctAnswer) {
                    score++;
                }
            }

            // 6️⃣ Generate weakness summary (only for wrong answers)
            ArrayNode wrongQuestions = objectMapper.createArrayNode();
            for (int i = 0; i < totalQuestions; i++) {
                JsonNode questionNode = questionsArray.get(i);
                int correctAnswer = questionNode.get("correctAnswer").asInt();
                if (i >= userAnswers.size() || userAnswers.get(i) != correctAnswer) {
                    wrongQuestions.add(questionNode);
                }
            }

            String weaknessSummary = "";
            if (wrongQuestions.size() > 0) {
                weaknessSummary = geminiService.analyzeWeakness(wrongQuestions.toString());
                quiz.setWeaknessSummary(weaknessSummary);
            }

            // 7️⃣ Save quiz with weakness summary
            quizRepository.save(quiz);

            // 8️⃣ Return response
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "score", score,
                    "totalQuestions", totalQuestions,
                    "weaknessSummary", weaknessSummary
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

}