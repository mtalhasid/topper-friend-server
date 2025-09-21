package com.backend.topperfriendweb.service.studyplan;

import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
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

    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) value;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof java.time.OffsetDateTime) return ((java.time.OffsetDateTime) value).toLocalDateTime();
        if (value instanceof java.time.ZonedDateTime) return ((java.time.ZonedDateTime) value).toLocalDateTime();
        try { return java.time.LocalDateTime.parse(value.toString()); } catch (Exception ignored) { return null; }
    }

    @Transactional(readOnly = true)
    public List<StudyPlanDTO> getUserStudyPlans(Long userId) {
        int limit = 50;
        int offset = 0;
        List<Object[]> rows = studyPlanRepository.findAllBasicByUser(userId, limit, offset);
        return rows.stream().map(r -> {
            StudyPlanDTO dto = new StudyPlanDTO();
            dto.setId(((Number) r[0]).longValue());
            dto.setPdfTitle((String) r[1]);
            String status = r[2] == null ? "active" : r[2].toString().toLowerCase();
            dto.setStatus(com.backend.topperfriendweb.model.StudyPlanStatus.valueOf(status));
            dto.setCreatedAt(toLocalDateTime(r[3]));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudyPlanDTO getStudyPlanById(Long studyPlanId, Long userId) {
        List<Object[]> rows = studyPlanRepository.findByIdBasic(studyPlanId, userId);
        if (rows.isEmpty()) throw new IllegalArgumentException("Study plan not found");
        Object[] r = rows.get(0);
        StudyPlanDTO dto = new StudyPlanDTO();
        dto.setId(((Number) r[0]).longValue());
        dto.setPdfUrl((String) r[1]);
        dto.setPdfTitle((String) r[2]);
        dto.setMarkdown((String) r[3]);
        dto.setResources((String) r[4]);
        dto.setTasks((String) r[5]);
        String status = r[6] == null ? "active" : r[6].toString().toLowerCase();
        dto.setStatus(com.backend.topperfriendweb.model.StudyPlanStatus.valueOf(status));
        dto.setCreatedAt(toLocalDateTime(r[7]));
        dto.setUpdatedAt(toLocalDateTime(r[8]));
        return dto;
    }
}
