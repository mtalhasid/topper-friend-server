package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanStatusRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTitleRequest;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plans")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateStudyPlan() {
        try {
            User user = getLoggedInUser();
            StudyPlanDTO studyPlan = studyPlanService.createStudyPlanFromLatestQuiz(user);
            return ResponseEntity.ok(Map.of("success", true, "studyPlan", studyPlan));
        } catch (Exception e) {
            log.error("Error generating study plan", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to create study plan: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllStudyPlans() {
        try {
            User user = getLoggedInUser();
            List<StudyPlanDTO> studyPlans = studyPlanService.getUserStudyPlans(user.getId());
            return ResponseEntity.ok(Map.of("success", true, "studyPlans", studyPlans));
        } catch (Exception e) {
            log.error("Error getting study plans", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudyPlanById(@PathVariable Long id) {
        try {
            User user = getLoggedInUser();
            StudyPlanDTO studyPlan = studyPlanService.getStudyPlanById(id, user.getId());
            return ResponseEntity.ok(Map.of("success", true, "studyPlan", studyPlan));
        } catch (Exception e) {
            log.error("Error getting study plan by ID", e);
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStudyPlanStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanStatusRequest request) {
        try {
            User user = getLoggedInUser();
            StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanStatus(id, request, user.getId());
            return ResponseEntity.ok(Map.of("success", true, "studyPlan", studyPlan));
        } catch (Exception e) {
            log.error("Error updating study plan status", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/title")
    public ResponseEntity<?> updateStudyPlanTitle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanTitleRequest request) {
        try {
            User user = getLoggedInUser();
            StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanTitle(id, request, user.getId());
            return ResponseEntity.ok(Map.of("success", true, "studyPlan", studyPlan));
        } catch (Exception e) {
            log.error("Error updating study plan title", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyPlan(@PathVariable Long id) {
        try {
            User user = getLoggedInUser();
            studyPlanService.deleteStudyPlan(id, user.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Study plan deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting study plan", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}