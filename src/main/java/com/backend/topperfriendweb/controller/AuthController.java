// src/main/java/com/backend/topperfriendweb/controller/AuthController.java
package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.*;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.service.AuthService;
import com.backend.topperfriendweb.utils.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil; 
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            String message = authService.register(req); // Now returns String, not Long

            Map<String, Object> response = new HashMap<>();
            response.put("message", message);
            response.put("requiresVerification", true);

            return ResponseEntity.status(200).body(response); // Changed to 200 OK
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Registration error", ex);
            return ResponseEntity.status(500).body(Map.of("message", "Registration failed"));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        try {
            Long userId = authService.verifyOtp(req.getEmail(), req.getCode());

            // Generate JWT token here in the controller
            String token = jwtUtil.generateToken(req.getEmail());

            // Return both userId and token
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verified successfully",
                    "userId", userId,
                    "token", token
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("OTP verification error", ex);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/onboarding")
    public ResponseEntity<?> onboarding(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody OnboardingRequest request) {
        try {
            logger.info("Received onboarding request for: {}", request.toString());
            String token = authHeader.replace("Bearer ", "");
            logger.info("Token received: {}", token);

            String email = jwtUtil.getEmailFromToken(token);
            logger.info("Email extracted from token: {}", email);

            User user = authService.getUserByEmail(email);
            logger.info("User found: {}", user.getId());

            User updatedUser = authService.completeOnboarding(user.getId(), request);
            logger.info("Onboarding completed for user: {}", updatedUser.getId());

            // FIXED: Use HashMap instead of Map.of() to handle null values
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", updatedUser.getId());
            userResponse.put("username", updatedUser.getUsername());
            userResponse.put("name", updatedUser.getName());
            userResponse.put("collegeName", updatedUser.getCollegeName());
            userResponse.put("rollNumber", updatedUser.getRollNumber());
            userResponse.put("image", updatedUser.getImage());
            userResponse.put("onboardingCompleted", updatedUser.getOnboardingCompleted());

            return ResponseEntity.ok(Map.of("user", userResponse));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error in onboarding: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Internal server error in onboarding: ", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    // inside AuthController.java
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        try {
            authService.resendOtp(request.getEmail());
            return ResponseEntity.ok(Map.of("success", true, "message", "New OTP sent successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Resend OTP error", ex);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to resend OTP"));
        }
    }

}
