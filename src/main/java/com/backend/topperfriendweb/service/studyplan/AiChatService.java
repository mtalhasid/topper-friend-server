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
        // Backward-compatible method: preserve signature by delegating with no overrides
        return chatWithStudyPlanWeakness(user, message, studyPlanId, (String) null);
    }

    public String chatWithStudyPlanWeakness(User user, String message, Long studyPlanId, String explicitWeakness) {
        StudyPlan studyPlan = studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new RuntimeException("Study plan not found"));
        if (studyPlan.getUser() == null || !studyPlan.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You do not have access to this study plan");
        }

        String weakness = null;

        // Priority 1: explicit weakness text provided
        if (explicitWeakness != null && !explicitWeakness.isBlank()) {
            weakness = explicitWeakness;
        } else {
            // Priority 2: derive structured context from study plan content
            String derived = buildPlanContext(studyPlan);
            if (derived != null && !derived.isBlank()) {
                weakness = derived;
            }
        }

        if (weakness == null || weakness.isBlank()) {
            throw new IllegalStateException("No weakness context available from this study plan. Provide a weakness explicitly.");
        }

        String prompt = "You are an expert AI study coach.\n" +
                "Context about the learner's study plan (use this as background, do not invent facts):\n" + weakness + "\n\n" +
                "Answer the user's question by:\n" +
                "1) If they ask 'what is my weakness' or similar, summarize their key weak areas from the context.\n" +
                "2) If the topic is outside plan scope (e.g., OLAP), give a concise primer (2-4 bullet points), 3 quick practice tips, and suggest how to integrate it into the current or next week of the plan.\n" +
                "3) Otherwise, provide a clear, stepwise answer tailored to their plan.\n" +
                "Formatting: Use brief headings and bullet points. End with 1 short follow-up question.\n\n" +
                "User: " + message;

        return callGeminiAPI(prompt);
    }

    // Build a concise, structured context from the study plan fields.
    private String buildPlanContext(StudyPlan studyPlan) {
        StringBuilder sb = new StringBuilder();
        if (studyPlan.getPdfTitle() != null && !studyPlan.getPdfTitle().isBlank()) {
            sb.append("- Plan title: ").append(studyPlan.getPdfTitle()).append("\n");
        }
        if (studyPlan.getMarkdown() != null && !studyPlan.getMarkdown().isBlank()) {
            String md = studyPlan.getMarkdown().trim();
            String mdSnippet = md.length() > 500 ? md.substring(0, 500) + "..." : md;
            sb.append("- Key topics (from markdown):\n").append(mdSnippet).append("\n");
        }
        if (studyPlan.getTasks() != null && !studyPlan.getTasks().isBlank()) {
            String tasks = studyPlan.getTasks().trim();
            String tasksSnippet = tasks.length() > 400 ? tasks.substring(0, 400) + "..." : tasks;
            sb.append("- Tasks focus: \n").append(tasksSnippet).append("\n");
        }
        if (studyPlan.getResources() != null && !studyPlan.getResources().isBlank()) {
            String res = studyPlan.getResources().trim();
            String resSnippet = res.length() > 300 ? res.substring(0, 300) + "..." : res;
            sb.append("- Resources: \n").append(resSnippet).append("\n");
        }
        return sb.toString().trim();
    }
}