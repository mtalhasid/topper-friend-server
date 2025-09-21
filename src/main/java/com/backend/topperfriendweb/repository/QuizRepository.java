package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUserId(Long userId);
    Optional<Quiz> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    // Lightweight list for UI: avoid TEXT columns (questions_json, weakness_summary)
    @Query(value = """
        SELECT q.id, q.title, q.total_questions, q.created_at, q.user_id,
               COALESCE(q.weakness_summary, '') AS weakness_summary,
               q.questions_json
        FROM quizzes q
        WHERE q.user_id = :userId
        ORDER BY q.created_at DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findAllBasicByUser(@Param("userId") Long userId,
                                       @Param("limit") int limit,
                                       @Param("offset") int offset);

    // Lightweight detail: include TEXT only when fetching a single quiz
    @Query(value = """
        SELECT q.id, q.title, q.questions_json, q.weakness_summary, q.total_questions, q.created_at, q.user_id
        FROM quizzes q
        WHERE q.id = :id
    """, nativeQuery = true)
    List<Object[]> findByIdBasic(@Param("id") Long id);

}
