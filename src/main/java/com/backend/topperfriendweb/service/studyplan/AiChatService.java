package com.backend.topperfriendweb.service.studyplan;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import com.backend.topperfriendweb.service.quiz.GeminiService;
import org.springframework.stereotype.Service;


@Service
public class AiChatService {
    private final StudyPlanRepository studyPlanRepository;
    private final GeminiService geminiService;

    private String callGeminiAPI(String prompt) {
        return geminiService.generateContent(prompt);
    }

    public AiChatService(StudyPlanRepository studyPlanRepository, GeminiService geminiService) {
        this.studyPlanRepository = studyPlanRepository;
        this.geminiService = geminiService;
    }

    public String chatWithStudyPlanWeakness(User user, String message, Long studyPlanId) {
        StudyPlan studyPlan = studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new RuntimeException("Study plan not found"));

        String weakness = studyPlan.getQuiz().getWeaknessSummary();

        String prompt = "You are an AI tutor. The user has these weaknesses: \n"
                + weakness + "\nAnswer their question/help them accordingly.\nUser: " + message;

        return callGeminiAPI(prompt);
    }
}