package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUserId(Long userId);
    Optional<Quiz> findTopByUserIdOrderByCreatedAtDesc(Long userId);


}
