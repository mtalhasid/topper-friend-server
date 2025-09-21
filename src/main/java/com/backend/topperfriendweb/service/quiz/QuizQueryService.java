package com.backend.topperfriendweb.service.quiz;

import com.backend.topperfriendweb.dto.quiz.QuizDTO;
import com.backend.topperfriendweb.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizQueryService {
    private final QuizRepository quizRepository;

    public QuizQueryService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> getUserQuizzes(Long userId) {
        // Default pagination for safety
        int limit = 50;
        int offset = 0;
        List<Object[]> rows = quizRepository.findAllBasicByUser(userId, limit, offset);
        return rows.stream().map(r -> {
            QuizDTO dto = new QuizDTO();
            dto.setId(((Number) r[0]).longValue());
            dto.setTitle((String) r[1]);
            dto.setTotalQuestions(((Number) r[2]).intValue());
            dto.setCreatedAt(toLocalDateTime(r[3]));
            dto.setUserId(((Number) r[4]).longValue());
            // Restore weakness summary display using lightweight preview column
            dto.setWeaknessSummary((String) r[5]);
            // Also include questions JSON for list as requested
            dto.setQuestionsJson((String) r[6]);
            // Do not set questionsJson/weaknessSummary in list
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizDTO getQuizById(Long quizId) {
        List<Object[]> rows = quizRepository.findByIdBasic(quizId);
        if (rows.isEmpty()) throw new IllegalArgumentException("Quiz not found");
        Object[] r = rows.get(0);
        QuizDTO dto = new QuizDTO();
        dto.setId(((Number) r[0]).longValue());
        dto.setTitle((String) r[1]);
        dto.setQuestionsJson((String) r[2]);
        dto.setWeaknessSummary((String) r[3]);
        dto.setTotalQuestions(((Number) r[4]).intValue());
        dto.setCreatedAt(toLocalDateTime(r[5]));
        dto.setUserId(((Number) r[6]).longValue());
        return dto;
    }

    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) value;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof java.time.OffsetDateTime) return ((java.time.OffsetDateTime) value).toLocalDateTime();
        if (value instanceof java.time.ZonedDateTime) return ((java.time.ZonedDateTime) value).toLocalDateTime();
        try { return java.time.LocalDateTime.parse(value.toString()); } catch (Exception ignored) { return null; }
    }
}
