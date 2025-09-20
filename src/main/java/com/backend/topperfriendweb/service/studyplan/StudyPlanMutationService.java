package com.backend.topperfriendweb.service.studyplan;

import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanStatusRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTitleRequest;
import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.StudyPlanStatus;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import com.backend.topperfriendweb.service.quiz.GeminiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class StudyPlanMutationService {
    private final StudyPlanRepository studyPlanRepository;
    private final GeminiService geminiService;

    public StudyPlanMutationService(StudyPlanRepository studyPlanRepository, GeminiService geminiService) {
        this.studyPlanRepository = studyPlanRepository;
        this.geminiService = geminiService;
    }

    @Transactional
    public StudyPlanDTO createStudyPlanFromWeaknessAnalysis(User user, String weaknessAnalysis) {
        String tasks = geminiService.generateStudyPlanTasks(weaknessAnalysis);

        StudyPlan plan = new StudyPlan();
        plan.setUser(user);
        plan.setQuiz(null);
        plan.setTasks(tasks);
        plan.setStatus(StudyPlanStatus.active);
        plan.setPdfTitle("Study Plan - " + new Date().toString().substring(0, 10));

        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanStatus(Long studyPlanId, UpdateStudyPlanStatusRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                        User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        plan.setStatus(request.getStatus());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanTitle(Long studyPlanId, UpdateStudyPlanTitleRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                        User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        plan.setPdfTitle(request.getTitle());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public void deleteStudyPlan(Long studyPlanId, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                        User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));
        studyPlanRepository.delete(plan);
    }
}
