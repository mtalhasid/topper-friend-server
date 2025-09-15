package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // NEW - RAW SQL FOR BROWSE WITH USER'S LIKE/SAVE STATUS
    @Query(value = """
    SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
           COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat,
           COALESCE(string_agg(DISTINCT nlu.user_id::text, ','), '') as liked_users_concat,
           COALESCE(string_agg(DISTINCT nsu.user_id::text, ','), '') as saved_users_concat
    FROM notes n 
    LEFT JOIN note_tags nt ON n.id = nt.note_id 
    LEFT JOIN note_liked_users nlu ON n.id = nlu.note_id
    LEFT JOIN note_saved_users nsu ON n.id = nsu.note_id
    WHERE (:query IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')))
    AND (:tag IS NULL OR EXISTS (SELECT 1 FROM note_tags nt2 WHERE nt2.note_id = n.id AND nt2.tag = :tag))
    GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
    ORDER BY n.created_at DESC 
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findNotesWithUserStatus(@Param("query") String query,
                                           @Param("tag") String tag,
                                           @Param("currentUserId") Long currentUserId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    // ORIGINAL RAW SQL FOR BROWSE - KEEP AS BACKUP
    @Query(value = """
        SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
               COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat
        FROM notes n 
        LEFT JOIN note_tags nt ON n.id = nt.note_id 
        WHERE (:query IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:tag IS NULL OR EXISTS (SELECT 1 FROM note_tags nt2 WHERE nt2.note_id = n.id AND nt2.tag = :tag))
        GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
        ORDER BY n.created_at DESC 
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findNotesRaw(@Param("query") String query,
                                @Param("tag") String tag,
                                @Param("limit") int limit,
                                @Param("offset") int offset);

    // RAW SQL FOR USER NOTES
    @Query(value = """
        SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
               COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat
        FROM notes n 
        LEFT JOIN note_tags nt ON n.id = nt.note_id 
        WHERE n.user_id = :userId
        GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
        ORDER BY n.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findUserNotesRaw(@Param("userId") Long userId);

    @Query(value = """
    SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
           COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat,
           COALESCE(string_agg(DISTINCT nlu.user_id::text, ','), '') as liked_users_concat,
           COALESCE(string_agg(DISTINCT nsu.user_id::text, ','), '') as saved_users_concat
    FROM notes n 
    LEFT JOIN note_tags nt ON n.id = nt.note_id 
    LEFT JOIN note_liked_users nlu ON n.id = nlu.note_id
    LEFT JOIN note_saved_users nsu ON n.id = nsu.note_id
    WHERE n.user_id = :userId
    GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
    ORDER BY n.created_at DESC
    """, nativeQuery = true)
    List<Object[]> findUserNotesWithArrays(@Param("userId") Long userId);

    // RAW SQL FOR LIKED NOTES
    @Query(value = """
        SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
               COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat
        FROM notes n 
        LEFT JOIN note_tags nt ON n.id = nt.note_id 
        JOIN note_liked_users nlu ON n.id = nlu.note_id
        WHERE nlu.user_id = :userId
        GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
        ORDER BY n.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findLikedNotesRaw(@Param("userId") Long userId);

    // RAW SQL FOR SAVED NOTES
    @Query(value = """
        SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
               COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat
        FROM notes n 
        LEFT JOIN note_tags nt ON n.id = nt.note_id 
        JOIN note_saved_users nsu ON n.id = nsu.note_id
        WHERE nsu.user_id = :userId
        GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
        ORDER BY n.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findSavedNotesRaw(@Param("userId") Long userId);

    // For liked notes
    @Query(value = """
    SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
           COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat,
           COALESCE(string_agg(DISTINCT nlu.user_id::text, ','), '') as liked_users_concat,
           COALESCE(string_agg(DISTINCT nsu.user_id::text, ','), '') as saved_users_concat
    FROM notes n 
    LEFT JOIN note_tags nt ON n.id = nt.note_id 
    LEFT JOIN note_liked_users nlu ON n.id = nlu.note_id
    LEFT JOIN note_saved_users nsu ON n.id = nsu.note_id
    JOIN note_liked_users nlu2 ON n.id = nlu2.note_id
    WHERE nlu2.user_id = :userId
    GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
    ORDER BY n.created_at DESC
    """, nativeQuery = true)
    List<Object[]> findLikedNotesWithArrays(@Param("userId") Long userId);

    // For saved notes
    @Query(value = """
    SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
           COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat,
           COALESCE(string_agg(DISTINCT nlu.user_id::text, ','), '') as liked_users_concat,
           COALESCE(string_agg(DISTINCT nsu.user_id::text, ','), '') as saved_users_concat
    FROM notes n 
    LEFT JOIN note_tags nt ON n.id = nt.note_id 
    LEFT JOIN note_liked_users nlu ON n.id = nlu.note_id
    LEFT JOIN note_saved_users nsu ON n.id = nsu.note_id
    JOIN note_saved_users nsu2 ON n.id = nsu2.note_id
    WHERE nsu2.user_id = :userId
    GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
    ORDER BY n.created_at DESC
    """, nativeQuery = true)
    List<Object[]> findSavedNotesWithArrays(@Param("userId") Long userId);

    // COUNT QUERY FOR PAGINATION
    @Query(value = """
        SELECT COUNT(DISTINCT n.id)
        FROM notes n 
        LEFT JOIN note_tags nt ON n.id = nt.note_id 
        WHERE (:query IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:tag IS NULL OR EXISTS (SELECT 1 FROM note_tags nt2 WHERE nt2.note_id = n.id AND nt2.tag = :tag))
        """, nativeQuery = true)
    Long countNotesRaw(@Param("query") String query, @Param("tag") String tag);

    // FAST TAGS QUERY
    @Query(value = "SELECT DISTINCT tag FROM note_tags ORDER BY tag", nativeQuery = true)
    List<String> findAllTagsFast();

    // KEEP YOUR EXISTING METHODS FOR BACKWARD COMPATIBILITY
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Note> findBySavedByUsersContainsOrderByCreatedAtDesc(Long userId);
    List<Note> findByLikedByUsersContainsOrderByCreatedAtDesc(Long userId);

    @Query("SELECT DISTINCT n.tags FROM Note n")
    List<String> findAllDistinctTags();

    @Query("SELECT n FROM Note n WHERE " +
            "(:query IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR :query MEMBER OF n.tags) " +
            "AND (:tag IS NULL OR :tag MEMBER OF n.tags)")
    List<Note> searchNotes(@Param("query") String query, @Param("tag") String tag);

    @Query("SELECT COALESCE(SUM(n.likes), 0) FROM Note n WHERE n.userId = :userId")
    Long getTotalLikesByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);
}