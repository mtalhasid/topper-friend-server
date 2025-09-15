package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.quiz.QuizDTO;
import com.backend.topperfriendweb.dto.quiz.QuizSubmissionResponse;
import com.backend.topperfriendweb.dto.quiz.SubmitQuizRequest;
import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {
    private final QuizRepository quizRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<QuizDTO> getUserQuizzes(Long userId) {
        return quizRepository.findByUserId(userId)
                .stream()
                .map(QuizDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizDTO getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return new QuizDTO(quiz);
    }

    @Transactional
    public QuizDTO createQuiz(String title, String questionsJson, User user) {
        try {
            // Parse JSON to get total questions count
            JsonNode quizArray = objectMapper.readTree(questionsJson);
            if (!quizArray.isArray()) {
                throw new RuntimeException("Invalid quiz format: expected JSON array");
            }

            Quiz quiz = new Quiz();
            quiz.setTitle(title);
            quiz.setQuestionsJson(questionsJson);
            quiz.setTotalQuestions(quizArray.size());
            quiz.setUser(user);

            Quiz savedQuiz = quizRepository.save(quiz);
            return new QuizDTO(savedQuiz);
        } catch (Exception e) {
            log.error("Error creating quiz", e);
            throw new RuntimeException("Failed to create quiz: " + e.getMessage());
        }
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(SubmitQuizRequest request, User user) {
        try {
            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));

            // Parse questions JSON
            JsonNode questionsArray = objectMapper.readTree(quiz.getQuestionsJson());
            if (!questionsArray.isArray()) {
                throw new RuntimeException("Invalid quiz format");
            }

            // Calculate score
            int score = 0;
            int totalQuestions = questionsArray.size();
            ArrayNode wrongQuestions = objectMapper.createArrayNode();

            for (int i = 0; i < totalQuestions; i++) {
                JsonNode questionNode = questionsArray.get(i);
                int correctAnswer = questionNode.get("correctAnswer").asInt();
                
                if (i < request.getAnswers().size() && request.getAnswers().get(i) == correctAnswer) {
                    score++;
                } else {
                    wrongQuestions.add(questionNode);
                }
            }

            // Generate weakness summary for wrong answers
            String weaknessSummary = "";
            if (wrongQuestions.size() > 0) {
                try {
                    weaknessSummary = geminiService.analyzeWeakness(wrongQuestions.toString());
                    quiz.setWeaknessSummary(weaknessSummary);
                    quizRepository.save(quiz);
                } catch (Exception e) {
                    log.warn("Failed to generate weakness summary", e);
                    weaknessSummary = "Unable to generate weakness analysis at this time.";
                }
            }

            return new QuizSubmissionResponse(
                    true,
                    score,
                    totalQuestions,
                    weaknessSummary,
                    "Quiz submitted successfully"
            );

        } catch (Exception e) {
            log.error("Error submitting quiz", e);
            throw new RuntimeException("Failed to submit quiz: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (!quiz.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this quiz");
        }

        quizRepository.delete(quiz);
    }
}
