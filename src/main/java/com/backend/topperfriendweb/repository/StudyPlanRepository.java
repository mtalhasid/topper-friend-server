package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findByUserId(Long userId);
    Optional<StudyPlan> findByIdAndUser(Long id, User user);
    // ✅ Find latest study plan for a user
    Optional<StudyPlan> findTopByUserIdOrderByCreatedAtDesc(Long userId);

}
