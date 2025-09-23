package com.backend.topperfriendweb.service.studyplan;

import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanStatusRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTitleRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanMarkdownRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanResourcesRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTasksRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanPdfRequest;
import com.backend.topperfriendweb.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyPlanService {

    private final StudyPlanQueryService studyPlanQueryService;
    private final StudyPlanMutationService studyPlanMutationService;

    @Transactional
    public StudyPlanDTO createStudyPlanFromWeaknessAnalysis(User user, String weaknessAnalysis) {
        return studyPlanMutationService.createStudyPlanFromWeaknessAnalysis(user, weaknessAnalysis);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanDTO> getUserStudyPlans(Long userId) {
        return studyPlanQueryService.getUserStudyPlans(userId);
    }

    @Transactional(readOnly = true)
    public StudyPlanDTO getStudyPlanById(Long studyPlanId, Long userId) {
        return studyPlanQueryService.getStudyPlanById(studyPlanId, userId);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanStatus(Long studyPlanId, UpdateStudyPlanStatusRequest request, Long userId) {
        return studyPlanMutationService.updateStudyPlanStatus(studyPlanId, request, userId);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanTitle(Long studyPlanId, UpdateStudyPlanTitleRequest request, Long userId) {
        return studyPlanMutationService.updateStudyPlanTitle(studyPlanId, request, userId);
    }

    @Transactional
    public void deleteStudyPlan(Long studyPlanId, Long userId) {
        studyPlanMutationService.deleteStudyPlan(studyPlanId, userId);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanMarkdown(Long studyPlanId, UpdateStudyPlanMarkdownRequest request, Long userId) {
        return studyPlanMutationService.updateStudyPlanMarkdown(studyPlanId, request, userId);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanResources(Long studyPlanId, UpdateStudyPlanResourcesRequest request, Long userId) {
        return studyPlanMutationService.updateStudyPlanResources(studyPlanId, request, userId);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanTasks(Long studyPlanId, UpdateStudyPlanTasksRequest request, Long userId) {
        return studyPlanMutationService.updateStudyPlanTasks(studyPlanId, request, userId);
    }

    @Transactional
    public StudyPlanDTO updateStudyPlanPdf(Long studyPlanId, UpdateStudyPlanPdfRequest request, Long userId) {
        return studyPlanMutationService.updateStudyPlanPdf(studyPlanId, request, userId);
    }
}