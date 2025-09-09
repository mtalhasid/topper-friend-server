package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Value; // ✅ correct
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {
    private final WebClient webClient;
    private final QuizRepository quizRepository; // ✅ Inject repository

    @Value("${gemini.apiKey}")
    private String apiKey;

    public GeminiService(WebClient.Builder builder, QuizRepository quizRepository) {
        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
        this.quizRepository = quizRepository; // assign injected repo
    }

    public String summarize(String text) {
        String prompt = "Please summarize the following text in a clear and concise manner:\n" + text;
        return callGemini(prompt);
    }

    public List<Quiz> generateQuiz(String text, User user) {
        try {
            // Prompt AI to generate quiz in plain text / Markdown
            String prompt = "Generate a quiz in plain text format with 2-3 multiple choice questions for: "
                    + text
                    + "\nFormat like this:\n"
                    + "**Question 1:** ...\n"
                    + "a) Option A\n"
                    + "b) Option B\n"
                    + "c) Option C\n"
                    + "d) Option D\n"
                    + "**Answer Key:**\n1: b\n2: c ...";

            // Call Gemini AI
            String response = callGemini(prompt);

            // Store AI response directly, no JSON parsing
            Quiz quiz = new Quiz();
            quiz.setUser(user);
            quiz.setTitle("Generated Quiz");
            quiz.setQuestionsJson(response); // raw Markdown/plain text
            quiz.setTotalQuestions(3); // or however many questions you expect

            // ✅ Save using instance repository
            quizRepository.save(quiz);

            return List.of(quiz);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate quiz: " + e.getMessage(), e);
        }
    }

    private String callGemini(String prompt) {
        Map<String, Object> request = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt)))));

        Map response = webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List candidates = (List) response.get("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            if (parts != null && !parts.isEmpty()) {
                return (String) ((Map) parts.get(0)).get("text");
            }
        }
        return "";
    }

    public String analyzeWeakness(String wrongQuestionsJson) {
        String prompt = "Analyze the following incorrect quiz answers and give a brief summary of the user's weakness and how to improve:\n"
                + wrongQuestionsJson;
        return callGemini(prompt);
    }

    // Add this method to your existing GeminiService
    public String generateStudyPlanTasks(String weaknessAnalysis) {
        String prompt = "Generate a 4-week study plan with tasks based on this weakness analysis:\n" + weaknessAnalysis;
        return callGemini(prompt);
    }

}
