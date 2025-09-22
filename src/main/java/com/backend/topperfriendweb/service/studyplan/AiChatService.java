package com.backend.topperfriendweb.service.studyplan;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.backend.topperfriendweb.service.quiz.GeminiService;
import org.springframework.stereotype.Service;


@Service
public class AiChatService {
    private final StudyPlanRepository studyPlanRepository;
    private final QuizRepository quizRepository;
    private final GeminiService geminiService;

    private String callGeminiAPI(String prompt) {
        return geminiService.generateContent(prompt);
    }

    public AiChatService(StudyPlanRepository studyPlanRepository, QuizRepository quizRepository, GeminiService geminiService) {
        this.studyPlanRepository = studyPlanRepository;
        this.quizRepository = quizRepository;
        this.geminiService = geminiService;
    }

    public String chatWithStudyPlanWeakness(User user, String message, Long studyPlanId) {
        // Backward-compatible method: preserve signature by delegating with no overrides
        return chatWithStudyPlanWeakness(user, message, studyPlanId, null, null);
    }

    public String chatWithStudyPlanWeakness(User user, String message, Long studyPlanId, Long quizId, String explicitWeakness) {
        StudyPlan studyPlan = studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new RuntimeException("Study plan not found"));

        String weakness = null;
        Quiz quiz = null;

        // Priority 1: explicit weakness text provided
        if (explicitWeakness != null && !explicitWeakness.isBlank()) {
            weakness = explicitWeakness;
        } else {
            // Priority 2: explicit quizId provided
            if (quizId != null) {
                quiz = quizRepository.findById(quizId)
                        .orElseThrow(() -> new IllegalStateException("Quiz not found for provided quizId"));
                if (!quiz.getUser().getId().equals(user.getId())) {
                    throw new IllegalStateException("You do not have access to this quiz");
                }
                weakness = quiz.getWeaknessSummary();
                // Persist the association for future calls if not already linked
                if (quiz != null && (studyPlan.getQuiz() == null || !quiz.equals(studyPlan.getQuiz()))) {
                    studyPlan.setQuiz(quiz);
                    studyPlanRepository.save(studyPlan);
                }
            } else {
                // Priority 3: study plan's linked quiz
                quiz = studyPlan.getQuiz();
                if (quiz != null) {
                    if (!quiz.getUser().getId().equals(user.getId())) {
                        throw new IllegalStateException("You do not have access to this study plan's quiz");
                    }
                    weakness = quiz.getWeaknessSummary();
                }
            }

            if ((weakness == null || weakness.isBlank())) {
                // If a quiz exists but has no weakness summary, derive a minimal context
                if (quiz != null) {
                    String fallback = quiz.getTitle();
                    if (fallback != null && !fallback.isBlank()) {
                        weakness = "Focus on topics related to this quiz: '" + fallback + "'";
                    } else {
                        throw new IllegalStateException("No weakness summary available for this study plan's quiz. Provide a weakness explicitly.");
                    }
                } else {
                    // No quiz linked at all
                    throw new IllegalStateException("No quiz linked to this study plan. Provide a quizId or a weakness explicitly.");
                }
            }
        }

        String prompt = "You are an AI tutor. The user has these weaknesses: \n"
                + weakness + "\nAnswer their question/help them accordingly.\nUser: " + message;

        return callGeminiAPI(prompt);
    }
}