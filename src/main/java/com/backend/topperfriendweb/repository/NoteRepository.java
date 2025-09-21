package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    //- RAW SQL FOR BROWSE WITH USER'S LIKE/SAVE STATUS
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

    // Bulk load notes by IDs with aggregated fields for mapping via NoteMapper.rawToDTO
    @Query(value = """
    SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username,
           COALESCE(string_agg(DISTINCT nt.tag, ','), '') as tags_concat,
           COALESCE(string_agg(DISTINCT nlu.user_id::text, ','), '') as liked_users_concat,
           COALESCE(string_agg(DISTINCT nsu.user_id::text, ','), '') as saved_users_concat
    FROM notes n
    LEFT JOIN note_tags nt ON n.id = nt.note_id
    LEFT JOIN note_liked_users nlu ON n.id = nlu.note_id
    LEFT JOIN note_saved_users nsu ON n.id = nsu.note_id
    WHERE n.id IN (:ids)
    GROUP BY n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
    """, nativeQuery = true)
    List<Object[]> findNotesByIds(@Param("ids") List<Long> ids);

    // Lightweight bulk loader for collections page (no joins)
    @Query(value = """
    SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
    FROM notes n
    WHERE n.id IN (:ids)
    """, nativeQuery = true)
    List<Object[]> findNotesByIdsBasic(@Param("ids") List<Long> ids);

    // Lightweight fetch of all notes for a user (no joins) for fast profile page
    @Query(value = """
    SELECT n.id, n.user_id, n.title, n.pdf_link, n.likes, n.created_at, n.updated_at, n.username
    FROM notes n
    WHERE n.user_id = :userId
    ORDER BY n.created_at DESC
    """, nativeQuery = true)
    List<Object[]> findUserNotesBasic(@Param("userId") Long userId);
}