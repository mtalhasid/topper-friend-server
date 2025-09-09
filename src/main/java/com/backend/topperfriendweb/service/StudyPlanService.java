package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final QuizRepository quizRepository;
    private final GeminiService geminiService;

    public StudyPlanService(StudyPlanRepository studyPlanRepository,
                            QuizRepository quizRepository,
                            GeminiService geminiService) {
        this.studyPlanRepository = studyPlanRepository;
        this.quizRepository = quizRepository;
        this.geminiService = geminiService;
    }

    // ✅ Creates a StudyPlan from latest quiz weakness
    public StudyPlan createStudyPlanFromLatestQuiz(User user) {
        // Get latest quiz weakness
        String weakness = quizRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(q -> q.getWeaknessSummary())
                .orElse("No weakness recorded yet.");

        // Generate 4-week study plan tasks using AI
        String tasks = geminiService.generateStudyPlanTasks(weakness);

        // Create StudyPlan
        StudyPlan plan = new StudyPlan();
        plan.setUser(user);
        plan.setTasks(tasks);          // <-- save AI-generated plan
        plan.setStatus(com.backend.topperfriendweb.model.StudyPlanStatus.active);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        return studyPlanRepository.save(plan);
    }

    public StudyPlan getLatestStudyPlan(User user) {
        return studyPlanRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId()).orElse(null);
    }
}
