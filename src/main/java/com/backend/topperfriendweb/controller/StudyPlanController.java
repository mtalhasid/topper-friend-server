package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.studyplan.StudyPlanDTO;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanStatusRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTitleRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanMarkdownRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanResourcesRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanTasksRequest;
import com.backend.topperfriendweb.dto.studyplan.UpdateStudyPlanPdfRequest;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.studyplan.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plans")
@PreAuthorize("isAuthenticated()")
@Validated
@RequiredArgsConstructor
@Slf4j
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping("/generate")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> generateStudyPlan(
            @RequestBody Map<String, String> request) {
        User user = getLoggedInUser();
        String weaknessAnalysis = request.get("weaknessAnalysis");

        if (weaknessAnalysis == null || weaknessAnalysis.trim().isEmpty()) {
            throw new IllegalArgumentException("Weakness analysis is required");
        }

        StudyPlanDTO studyPlan = studyPlanService.createStudyPlanFromWeaknessAnalysis(user, weaknessAnalysis);
        return ResponseEntity.ok(
                CommonResponse.success("Study plan generated successfully", studyPlan)
        );
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<StudyPlanDTO>>> getAllStudyPlans() {
        User user = getLoggedInUser();
        List<StudyPlanDTO> studyPlans = studyPlanService.getUserStudyPlans(user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plans retrieved successfully", studyPlans)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> getStudyPlanById(@PathVariable Long id) {
        User user = getLoggedInUser();
        StudyPlanDTO studyPlan = studyPlanService.getStudyPlanById(id, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan retrieved successfully", studyPlan)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> updateStudyPlanStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanStatusRequest request) {
        User user = getLoggedInUser();
        StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanStatus(id, request, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan status updated successfully", studyPlan)
        );
    }

    @PatchMapping("/{id}/title")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> updateStudyPlanTitle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanTitleRequest request) {
        User user = getLoggedInUser();
        StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanTitle(id, request, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan title updated successfully", studyPlan)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> deleteStudyPlan(@PathVariable Long id) {
        User user = getLoggedInUser();
        studyPlanService.deleteStudyPlan(id, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan deleted successfully", "Study plan deleted successfully")
        );
    }

    // New endpoints to update content fields
    @PatchMapping("/{id}/markdown")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> updateStudyPlanMarkdown(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanMarkdownRequest request) {
        User user = getLoggedInUser();
        StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanMarkdown(id, request, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan markdown updated successfully", studyPlan)
        );
    }

    @PatchMapping("/{id}/resources")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> updateStudyPlanResources(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanResourcesRequest request) {
        User user = getLoggedInUser();
        StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanResources(id, request, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan resources updated successfully", studyPlan)
        );
    }

    @PatchMapping("/{id}/tasks")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> updateStudyPlanTasks(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanTasksRequest request) {
        User user = getLoggedInUser();
        StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanTasks(id, request, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan tasks updated successfully", studyPlan)
        );
    }

    @PatchMapping("/{id}/pdf")
    public ResponseEntity<CommonResponse<StudyPlanDTO>> updateStudyPlanPdf(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudyPlanPdfRequest request) {
        User user = getLoggedInUser();
        StudyPlanDTO studyPlan = studyPlanService.updateStudyPlanPdf(id, request, user.getId());
        return ResponseEntity.ok(
                CommonResponse.success("Study plan PDF updated successfully", studyPlan)
        );
    }
}