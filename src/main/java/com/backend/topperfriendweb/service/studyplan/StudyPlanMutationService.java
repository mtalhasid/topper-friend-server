package com.backend.topperfriendweb.service.studyplan;

import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanStatusRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTitleRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanMarkdownRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanResourcesRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTasksRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanPdfRequest;
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

    @Transactional
    public StudyPlanDTO updateStudyPlanMarkdown(Long studyPlanId, UpdateStudyPlanMarkdownRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                        User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        plan.setMarkdown(request.getMarkdown());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanResources(Long studyPlanId, UpdateStudyPlanResourcesRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                        User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        plan.setResources(request.getResources());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanTasks(Long studyPlanId, UpdateStudyPlanTasksRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                        User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        plan.setTasks(request.getTasks());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanPdf(Long studyPlanId, UpdateStudyPlanPdfRequest request, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId,
                        User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        plan.setPdfUrl(request.getPdfUrl());
        plan.setPdfTitle(request.getPdfTitle());
        StudyPlan savedPlan = studyPlanRepository.save(plan);
        return new StudyPlanDTO(savedPlan);
    }
}
