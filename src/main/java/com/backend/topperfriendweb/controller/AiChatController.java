package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.studyplan.AiChatService;
import com.backend.topperfriendweb.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/ai-chat")
public class AiChatController {

    private final AiChatService aiChatService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AiChatController(AiChatService aiChatService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.aiChatService = aiChatService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/{studyPlanId}")  // Change this line
    public ResponseEntity<?> chat(@PathVariable Long studyPlanId,  // Add this parameter
                                  @RequestHeader("Authorization") String authHeader,
                                  @RequestBody Map<String, String> body) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.getEmailFromToken(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String message = body.get("message");
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }

            String aiResponse = aiChatService.chatWithStudyPlanWeakness(user, message, studyPlanId);  // Pass studyPlanId
            return ResponseEntity.ok(Map.of("success", true, "response", aiResponse));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
