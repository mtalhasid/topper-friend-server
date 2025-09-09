// src/main/java/com/backend/topperfriendweb/repository/NoteRepository.java
package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Note> findBySavedByUsersContainsOrderByCreatedAtDesc(Long userId);

    List<Note> findByLikedByUsersContainsOrderByCreatedAtDesc(Long userId); // 👈 ADD THIS

    @Query("SELECT DISTINCT n.tags FROM Note n")
    List<String> findAllDistinctTags();

    @Query("SELECT n FROM Note n WHERE " +
            "(:query IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR :query MEMBER OF n.tags) " +
            "AND (:tag IS NULL OR :tag MEMBER OF n.tags)")
    List<Note> searchNotes(@Param("query") String query, @Param("tag") String tag);

    long countByUserId(Long userId);
}
