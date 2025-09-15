package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.userprofile.UserProfileDTO;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    // Get all users with completed onboarding
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsersWithCompletedOnboarding();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch users"));
        }
    }

    // Get user profile by username WITH their notes
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            UserProfileDTO userProfile = userService.getUserProfileByUsername(username);
            return ResponseEntity.ok(userProfile);
        } catch (RuntimeException e) {
            log.error("Error getting user profile", e);
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting user profile", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch user profile"));
        }
    }

    // Search users by username or name
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        try {
            List<User> users = userService.searchUsers(q);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error searching users", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to search users"));
        }
    }

    // Get users by college
    @GetMapping("/college/{collegeName}")
    public ResponseEntity<?> getUsersByCollege(@PathVariable String collegeName) {
        try {
            List<User> users = userService.getUsersByCollege(collegeName);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users by college", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch users by college"));
        }
    }
}