package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.StudyPlanDTO;
import com.backend.topperfriendweb.dto.UpdateStudyPlanStatusRequest;
import com.backend.topperfriendweb.dto.UpdateStudyPlanTitleRequest;
import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.StudyPlanStatus;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final QuizRepository quizRepository;
    private final GeminiService geminiService;

    @Transactional
    public StudyPlanDTO createStudyPlanFromLatestQuiz(User user) {
        try {
            // Get latest quiz weakness
            String weakness = quizRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                    .map(q -> q.getWeaknessSummary())
                    .orElse("No weakness recorded yet.");

            // Generate 4-week study plan tasks using AI
            String tasks = geminiService.generateStudyPlanTasks(weakness);

            // Create StudyPlan
            StudyPlan plan = new StudyPlan();
            plan.setUser(user);
            plan.setTasks(tasks);
            plan.setStatus(StudyPlanStatus.active);

            StudyPlan savedPlan = studyPlanRepository.save(plan);
            return new StudyPlanDTO(savedPlan);
        } catch (Exception e) {
            log.error("Error creating study plan from quiz", e);
            throw new RuntimeException("Failed to create study plan: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public StudyPlanDTO getLatestStudyPlan(User user) {
        StudyPlan plan = studyPlanRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElse(null);
        return plan != null ? new StudyPlanDTO(plan) : null;
    }

    @Transactional(readOnly = true)
    public List<StudyPlanDTO> getUserStudyPlans(Long userId) {
        return studyPlanRepository.findByUserId(userId)
                .stream()
                .map(StudyPlanDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudyPlanDTO getStudyPlanById(Long studyPlanId, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId, 
                User.builder().id(userId).build())
                .orElseThrow(() -> new RuntimeException("Study plan not found"));
        return new StudyPlanDTO(plan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanStatus(Long studyPlanId, UpdateStudyPlanStatusRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId, 
                User.builder().id(userId).build())
                .orElseThrow(() -> new RuntimeException("Study plan not found"));

        plan.setStatus(request.getStatus());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanTitle(Long studyPlanId, UpdateStudyPlanTitleRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId, 
                User.builder().id(userId).build())
                .orElseThrow(() -> new RuntimeException("Study plan not found"));

        plan.setPdfTitle(request.getTitle());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public void deleteStudyPlan(Long studyPlanId, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId, 
                User.builder().id(userId).build())
                .orElseThrow(() -> new RuntimeException("Study plan not found"));

        studyPlanRepository.delete(plan);
    }
}
