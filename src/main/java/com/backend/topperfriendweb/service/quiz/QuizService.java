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
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        return new QuizDTO(quiz);
    }

    @Transactional
    public QuizDTO createQuiz(String title, String questionsJson, User user) {
        try {
            // Parse JSON to get total questions count
            JsonNode quizArray = objectMapper.readTree(questionsJson);
            if (!quizArray.isArray()) {
                throw new IllegalArgumentException("Invalid quiz format: expected JSON array");
            }

            if (quizArray.size() == 0) {
                throw new IllegalArgumentException("Quiz must contain at least one question");
            }

            Quiz quiz = new Quiz();
            quiz.setTitle(title);
            quiz.setQuestionsJson(questionsJson);
            quiz.setTotalQuestions(quizArray.size());
            quiz.setUser(user);

            Quiz savedQuiz = quizRepository.save(quiz);
            return new QuizDTO(savedQuiz);
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw IllegalArgumentException as-is
        } catch (Exception e) {
            log.error("Error creating quiz", e);
            throw new IllegalArgumentException("Failed to create quiz: " + e.getMessage());
        }
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(SubmitQuizRequest request, User user) {
        try {
            Quiz quiz = quizRepository.findById(request.getQuizId())
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

            // Parse questions JSON
            JsonNode questionsArray = objectMapper.readTree(quiz.getQuestionsJson());
            if (!questionsArray.isArray()) {
                throw new IllegalArgumentException("Invalid quiz format");
            }

            int totalQuestions = questionsArray.size();

            // Validate answers array length
            if (request.getAnswers() == null || request.getAnswers().size() != totalQuestions) {
                throw new IllegalArgumentException("Number of answers must match number of questions");
            }

            // Calculate score
            int score = 0;
            ArrayNode wrongQuestions = objectMapper.createArrayNode();

            for (int i = 0; i < totalQuestions; i++) {
                JsonNode questionNode = questionsArray.get(i);
                if (!questionNode.has("correctAnswer")) {
                    throw new IllegalArgumentException("Invalid question format: missing correctAnswer field");
                }

                int correctAnswer = questionNode.get("correctAnswer").asInt();
                Integer userAnswer = request.getAnswers().get(i);

                if (userAnswer != null && userAnswer.equals(correctAnswer)) {
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
            } else {
                weaknessSummary = "Excellent work! You answered all questions correctly.";
            }

            return new QuizSubmissionResponse(
                    true,
                    score,
                    totalQuestions,
                    weaknessSummary,
                    String.format("Quiz submitted successfully. You scored %d out of %d.", score, totalQuestions)
            );

        } catch (IllegalArgumentException e) {
            throw e; // Re-throw IllegalArgumentException as-is
        } catch (Exception e) {
            log.error("Error submitting quiz", e);
            throw new IllegalArgumentException("Failed to submit quiz: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        if (!quiz.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You don't have permission to delete this quiz");
        }

        quizRepository.delete(quiz);
    }
}