package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
@Service
public class AiChatService {

    private final QuizRepository quizRepository;
    private final WebClient webClient;

    @Value("${gemini.apiKey}")
    private String apiKey;

    public AiChatService(QuizRepository quizRepository, WebClient.Builder builder) {
        this.quizRepository = quizRepository;
        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public String chatWithUserWeakness(User user, String message) {
        String weakness = quizRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(quiz -> quiz.getWeaknessSummary())
                .orElse("No weakness recorded yet.");

        String prompt = "You are an AI tutor. The user has these weaknesses: \n"
                + weakness + "\nAnswer their question/help them accordingly.\nUser: " + message;

        Map<String, Object> request = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))))
        );

        Map response = webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null) {
            List candidates = (List) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List parts = (List) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) ((Map) parts.get(0)).get("text");
                }
            }
        }

        return "No response from AI";
    }
}
