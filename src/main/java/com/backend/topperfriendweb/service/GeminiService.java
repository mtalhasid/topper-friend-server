package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {
    private final WebClient webClient;
    private final QuizRepository quizRepository;

    @Value("${gemini.apiKey}")
    private String apiKey;

    public GeminiService(WebClient.Builder builder, QuizRepository quizRepository) {
        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
        this.quizRepository = quizRepository;
    }

    public String summarize(String text) {
        String prompt = "Please summarize the following text in a clear and concise manner:\n" + text;
        return callGemini(prompt);
    }

    // Add this method to GeminiService.java
    public String generateQuizJson(String text) {
        String prompt = "Generate a multiple-choice quiz in strict JSON format like this, Generate a quiz in strict JSON format ONLY. Do NOT include ```json or markdown formatting. Just raw JSON array.\n:\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"What is 2+2?\",\n" +
                "    \"options\": [\"1\", \"2\", \"3\", \"4\"],\n" +
                "    \"correctAnswer\": 3\n" +
                "  },\n" +
                "  {\n" +
                "    \"question\": \"Which is the largest planet?\",\n" +
                "    \"options\": [\"Earth\", \"Mars\", \"Jupiter\", \"Venus\"],\n" +
                "    \"correctAnswer\": 2\n" +
                "  }\n" +
                "]\n\n" +
                "Please generate 2-3 similar questions based on the following text:\n" + text;

        // Call Gemini AI
        return callGemini(prompt);
    }

    private String callGemini(String prompt) {
        try {
            Map<String, Object> request = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt)))));

            Map response = webClient.post()
                    .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is5xxServerError(),
                            clientResponse -> Mono.error(new RuntimeException("Google API service unavailable (503). Please try again later."))
                    )
                    .onStatus(
                            status -> status.is4xxClientError(),
                            clientResponse -> Mono.error(new RuntimeException("Invalid API request (4xx). Check your API key and quota."))
                    )
                    .bodyToMono(Map.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                    .timeout(Duration.ofSeconds(30))
                    .block();

            // Parse response
            List candidates = (List) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                if (content != null) {
                    List parts = (List) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map part = (Map) parts.get(0);
                        String text = (String) part.get("text");
                        if (text != null && !text.trim().isEmpty()) {
                            return text;
                        }
                    }
                }
            }

            // Handle empty response
            throw new RuntimeException("Received empty response from Gemini API");

        } catch (WebClientResponseException.ServiceUnavailable e) {
            throw new RuntimeException("Google's Gemini API is temporarily unavailable (503). Please try again in a few minutes.");
        } catch (WebClientResponseException.TooManyRequests e) {
            throw new RuntimeException("API rate limit exceeded. Please try again later.");
        } catch (WebClientResponseException e) {
            throw new RuntimeException("API error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (Exception e) {
            if (e.getMessage().contains("timeout")) {
                throw new RuntimeException("Request timed out. Google API may be slow - try again.");
            }
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    public String analyzeWeakness(String wrongQuestionsJson) {
        String prompt = "Analyze the following quiz content and provide a brief weakness analysis and study recommendations:\n"
                + wrongQuestionsJson;
        return callGemini(prompt);
    }

    public String generateStudyPlanTasks(String weaknessAnalysis) {
        String prompt = "Generate a 4-week study plan with specific tasks based on this weakness analysis:\n" + weaknessAnalysis;
        return callGemini(prompt);
    }
}