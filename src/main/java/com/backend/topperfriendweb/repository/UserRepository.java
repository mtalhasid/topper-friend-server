// src/main/java/com/backend/topperfriendweb/repository/UserRepository.java
package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    List<User> findByOnboardingCompletedTrue();

    List<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String username, String name);

    List<User> findByCollegeNameContainingIgnoreCase(String collegeName);

    // Lightweight list: return only fields needed for lists (no relationships)
    @Query(value = """
        SELECT id, username, name, image, college_name, created_at
        FROM users
        WHERE onboarding_completed = true
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> findAllBasic(@Param("limit") int limit, @Param("offset") int offset);

    // Lightweight profile by username (single row)
    @Query(value = """
        SELECT id, username, name, email, image, college_name, roll_number, onboarding_completed, created_at, updated_at
        FROM users
        WHERE username = :username
    """, nativeQuery = true)
    List<Object[]> findProfileBasicByUsername(@Param("username") String username);

    // Lightweight search
    @Query(value = """
        SELECT id, username, name, image, college_name, created_at
        FROM users
        WHERE lower(username) LIKE lower(CONCAT('%', :q, '%'))
           OR lower(name) LIKE lower(CONCAT('%', :q, '%'))
        ORDER BY created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> searchBasic(@Param("q") String q, @Param("limit") int limit);
}