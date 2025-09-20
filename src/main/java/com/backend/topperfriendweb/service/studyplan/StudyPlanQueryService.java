package com.backend.topperfriendweb.service.studyplan;

import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyPlanQueryService {
    private final StudyPlanRepository studyPlanRepository;

    public StudyPlanQueryService(StudyPlanRepository studyPlanRepository) {
        this.studyPlanRepository = studyPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyPlanDTO> getUserStudyPlans(Long userId) {
        List<StudyPlan> plans = studyPlanRepository.findByUserId(userId);
        return plans.stream().map(StudyPlanDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudyPlanDTO getStudyPlanById(Long studyPlanId, Long userId) {
        StudyPlan plan = studyPlanRepository.findByIdAndUser(studyPlanId, User.builder().id(userId).build())
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));
        return new StudyPlanDTO(plan);
    }
}
