package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.model.StudyPlan;
import com.backend.topperfriendweb.model.StudyPlanStatus;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.StudyPlanRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.StudyPlanService;
import com.backend.topperfriendweb.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plans")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final StudyPlanRepository studyPlanRepository;

    public StudyPlanController(StudyPlanService studyPlanService, JwtUtil jwtUtil,
                               UserRepository userRepository, StudyPlanRepository studyPlanRepository) {
        this.studyPlanService = studyPlanService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.studyPlanRepository = studyPlanRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateStudyPlan(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Create study plan from latest quiz (calls Gemini internally)
            StudyPlan plan = studyPlanService.createStudyPlanFromLatestQuiz(user);

            return ResponseEntity.ok(Map.of("success", true, "studyPlan", plan));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to create study plan: " + e.getMessage()));
        }
    }


    // ✅ Get all study plans for current user
    @GetMapping
    public ResponseEntity<?> getAllStudyPlans(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<StudyPlan> plans = studyPlanRepository.findByUserId(user.getId());
            return ResponseEntity.ok(Map.of("success", true, "studyPlans", plans));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Get a study plan by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudyPlanById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            StudyPlan plan = studyPlanRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Study plan not found"));

            return ResponseEntity.ok(Map.of("success", true, "studyPlan", plan));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Update study plan status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStudyPlanStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String statusStr = body.get("status");
            if (statusStr == null || statusStr.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
            }

            StudyPlan plan = studyPlanRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Study plan not found"));

            plan.setStatus(StudyPlanStatus.valueOf(statusStr));
            studyPlanRepository.save(plan);

            return ResponseEntity.ok(Map.of("success", true, "studyPlan", plan));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    // Add to StudyPlanController.java
// Update study plan title
    @PatchMapping("/{id}/title")
    public ResponseEntity<?> updateStudyPlanTitle(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String title = body.get("title");
            if (title == null || title.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "title is required"));
            }

            StudyPlan plan = studyPlanRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Study plan not found"));

            plan.setPdfTitle(title);
            plan.setUpdatedAt(LocalDateTime.now());
            studyPlanRepository.save(plan);

            return ResponseEntity.ok(Map.of("success", true, "studyPlan", plan));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Delete study plan
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyPlan(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            StudyPlan plan = studyPlanRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Study plan not found"));

            studyPlanRepository.delete(plan);

            return ResponseEntity.ok(Map.of("success", true, "message", "Study plan deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


}
