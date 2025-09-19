package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanStatusRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTitleRequest;
import com.backend.topperfriendweb.model.Quiz;
import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.StudyPlanStatus;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.QuizRepository;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
    public StudyPlanDTO createStudyPlanFromWeaknessAnalysis(User user, String weaknessAnalysis) {
        try {
            // Generate 4-week study plan tasks using AI with the specific weakness analysis
            String tasks = geminiService.generateStudyPlanTasks(weaknessAnalysis);

            // Create StudyPlan
            StudyPlan plan = new StudyPlan();
            plan.setUser(user);
            // Don't link to any specific quiz since this could be from any quiz
            plan.setQuiz(null);
            plan.setTasks(tasks);
            plan.setStatus(StudyPlanStatus.active);
            // You could set a title based on the weakness analysis
            plan.setPdfTitle("Study Plan - " + new Date().toString().substring(0, 10));

            StudyPlan savedPlan = studyPlanRepository.save(plan);
            log.info("Study plan created from weakness analysis for user: {}", user.getId());
            return new StudyPlanDTO(savedPlan);

        } catch (IllegalArgumentException e) {
            log.warn("Cannot create study plan for user {}: {}", user.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating study plan from weakness analysis for user: {}", user.getId(), e);
            throw new RuntimeException("Failed to create study plan: " + e.getMessage());
        }
    }

    @Transactional
    public StudyPlanDTO createStudyPlanFromLatestQuiz(User user) {
        try {
            // Get latest quiz (not just weakness)
            Quiz latestQuiz = quizRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("No quiz found to create study plan from"));

            String weakness = latestQuiz.getWeaknessSummary();
            if (weakness == null || weakness.isEmpty()) {
                weakness = "No weakness analysis available.";
            }

            // Generate 4-week study plan tasks using AI
            String tasks = geminiService.generateStudyPlanTasks(weakness);

            // Create StudyPlan
            StudyPlan plan = new StudyPlan();
            plan.setUser(user);
            plan.setQuiz(latestQuiz); // Link the quiz
            plan.setTasks(tasks);
            plan.setStatus(StudyPlanStatus.active);

            StudyPlan savedPlan = studyPlanRepository.save(plan);
            log.info("Study plan created successfully for user: {}", user.getId());
            return new StudyPlanDTO(savedPlan);

        } catch (IllegalArgumentException e) {
            log.warn("Cannot create study plan for user {}: {}", user.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating study plan from quiz for user: {}", user.getId(), e);
            throw new RuntimeException("Failed to create study plan: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<StudyPlanDTO> getUserStudyPlans(Long userId) {
        try {
            List<StudyPlan> plans = studyPlanRepository.findByUserId(userId);
            log.debug("Retrieved {} study plans for user: {}", plans.size(), userId);
            return plans.stream()
                    .map(StudyPlanDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving study plans for user: {}", userId, e);
            throw new RuntimeException("Failed to retrieve study plans");
        }
    }

    @Transactional(readOnly = true)
    public StudyPlanDTO getStudyPlanById(Long studyPlanId, Long userId) {
        try {
            StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                            User.builder().id(userId).build())
                    .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

            log.debug("Retrieved study plan: {} for user: {}", studyPlanId, userId);
            return new StudyPlanDTO(plan);

        } catch (IllegalArgumentException e) {
            log.warn("Study plan {} not found for user: {}", studyPlanId, userId);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving study plan {} for user: {}", studyPlanId, userId, e);
            throw new RuntimeException("Failed to retrieve study plan");
        }
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanStatus(Long studyPlanId, UpdateStudyPlanStatusRequest request, Long userId) {
        try {
            StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                            User.builder().id(userId).build())
                    .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

            StudyPlanStatus oldStatus = plan.getStatus();
            plan.setStatus(request.getStatus());
            StudyPlan savedPlan = studyPlanRepository.save(plan);

            log.info("Updated study plan {} status from {} to {} for user: {}",
                    studyPlanId, oldStatus, request.getStatus(), userId);

            return new StudyPlanDTO(savedPlan);

        } catch (IllegalArgumentException e) {
            log.warn("Cannot update status for study plan {} - user: {}: {}", studyPlanId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating study plan {} status for user: {}", studyPlanId, userId, e);
            throw new RuntimeException("Failed to update study plan status");
        }
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanTitle(Long studyPlanId, UpdateStudyPlanTitleRequest request, Long userId) {
        try {
            StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                            User.builder().id(userId).build())
                    .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

            String oldTitle = plan.getPdfTitle();
            plan.setPdfTitle(request.getTitle());
            StudyPlan savedPlan = studyPlanRepository.save(plan);

            log.info("Updated study plan {} title from '{}' to '{}' for user: {}",
                    studyPlanId, oldTitle, request.getTitle(), userId);

            return new StudyPlanDTO(savedPlan);

        } catch (IllegalArgumentException e) {
            log.warn("Cannot update title for study plan {} - user: {}: {}", studyPlanId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating study plan {} title for user: {}", studyPlanId, userId, e);
            throw new RuntimeException("Failed to update study plan title");
        }
    }

    @Transactional
    public void deleteStudyPlan(Long studyPlanId, Long userId) {
        try {
            StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                            User.builder().id(userId).build())
                    .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

            studyPlanRepository.delete(plan);
            log.info("Deleted study plan: {} for user: {}", studyPlanId, userId);

        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete study plan {} - user: {}: {}", studyPlanId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error deleting study plan {} for user: {}", studyPlanId, userId, e);
            throw new RuntimeException("Failed to delete study plan");
        }
    }
}