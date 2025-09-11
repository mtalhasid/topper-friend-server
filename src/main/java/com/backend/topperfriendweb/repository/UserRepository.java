// src/main/java/com/backend/topperfriendweb/repository/UserRepository.java
package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    List<User> findByOnboardingCompletedTrue();

    List<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String username, String name);

    List<User> findByCollegeNameContainingIgnoreCase(String collegeName);
}