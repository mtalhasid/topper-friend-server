// src/main/java/com/backend/topperfriendweb/controller/UserController.java
package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.model.Note;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteService noteService;

    // Get all users with completed onboarding
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findByOnboardingCompletedTrue();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch users"));
        }
    }

    // Get user profile by username WITH their notes
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty() || !userOptional.get().getOnboardingCompleted()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            User user = userOptional.get();

            // Get user's notes
            List<Note> userNotes = noteService.getUserNotesByUserId(user.getId());

            // Create response with user info + notes
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("notes", userNotes);
            response.put("totalNotes", userNotes.size());
            response.put("totalLikes", userNotes.stream().mapToInt(Note::getLikes).sum());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch user profile"));
        }
    }

    // Search users by username or name
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        try {
            List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(q, q);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to search users"));
        }
    }

    // Get users by college
    @GetMapping("/college/{collegeName}")
    public ResponseEntity<?> getUsersByCollege(@PathVariable String collegeName) {
        try {
            List<User> users = userRepository.findByCollegeNameContainingIgnoreCase(collegeName);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch users by college"));
        }
    }
}