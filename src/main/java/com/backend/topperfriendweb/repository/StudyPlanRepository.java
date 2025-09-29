package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findByUserId(Long userId);
    Optional<StudyPlan> findByIdAndUser(Long id, User user);
    // Find latest study plan for a user
    Optional<StudyPlan> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    // Quota helpers (derived)
    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);
    Optional<StudyPlan> findFirstByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(Long userId, LocalDateTime since);

    // Fast bulk load of study plans used in collections view (minimal necessary columns)
    @Query(value = """
        SELECT id, pdf_url, pdf_title, markdown, resources, tasks, status, created_at, updated_at
        FROM study_plans
        WHERE id IN (:ids)
    """, nativeQuery = true)
    List<Object[]> findStudyPlansBasicByIds(@Param("ids") List<Long> ids);

    // Ultra-light loader for collections page: skip large TEXT columns
    @Query(value = """
        SELECT id, pdf_title, status, created_at
        FROM study_plans
        WHERE id IN (:ids)
    """, nativeQuery = true)
    List<Object[]> findStudyPlansLiteByIds(@Param("ids") List<Long> ids);

    // Lightweight list for UI: id, title, status, created_at
    @Query(value = """
        SELECT id, pdf_title, status, created_at
        FROM study_plans
        WHERE user_id = :userId
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findAllBasicByUser(@Param("userId") Long userId,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    // Lightweight detail by id
    @Query(value = """
        SELECT id, pdf_url, pdf_title, markdown, resources, tasks, status, created_at, updated_at
        FROM study_plans
        WHERE id = :id AND user_id = :userId
    """, nativeQuery = true)
    List<Object[]> findByIdBasic(@Param("id") Long id, @Param("userId") Long userId);
}
