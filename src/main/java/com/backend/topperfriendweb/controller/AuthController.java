// src/main/java/com/backend/topperfriendweb/controller/AuthController.java
package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.auth.*;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.service.AuthService;
import com.backend.topperfriendweb.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            String message = authService.register(req);

            Map<String, Object> response = new HashMap<>();
            response.put("message", message);
            response.put("requiresVerification", true);

            return ResponseEntity.status(200).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(409).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Registration error", ex);
            return ResponseEntity.status(500).body(Map.of("message", "Registration failed"));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        try {
            Long userId = authService.verifyOtp(req.getEmail(), req.getCode());

            // Get the user to include more info in JWT
            User user = authService.getUserByEmail(req.getEmail());

            // Generate JWT token with user ID and email
            String token = jwtUtil.generateTokenWithUserInfo(user.getId(), user.getEmail(), user.getName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verified successfully",
                    "userId", userId,
                    "token", token
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("OTP verification error", ex);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());

            // Use enhanced login method
            Map<String, Object> response = authService.loginEnhanced(request.getEmail(), request.getPassword());

            if (!(Boolean) response.get("success")) {
                return ResponseEntity.status(401).body(response);
            }

            log.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Login error", ex);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Login failed: " + ex.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Get current user from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String email = (String) auth.getPrincipal();
                log.info("User {} logged out successfully", email);
            }

            // Clear security context
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logged out successfully"
            ));
        } catch (Exception ex) {
            log.error("Logout error", ex);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Logout failed"
            ));
        }
    }

    @PostMapping("/onboarding")
    public ResponseEntity<?> onboarding(@Valid @RequestBody OnboardingRequest request) {
        try {
            log.info("Received onboarding request for: {}", request.toString());

            // Get user from SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (String) auth.getPrincipal();
            log.info("Email from SecurityContext: {}", email);

            User user = authService.getUserByEmail(email);
            log.info("User found: {}", user.getId());

            User updatedUser = authService.completeOnboarding(user.getId(), request);
            log.info("Onboarding completed for user: {}", updatedUser.getId());

            // Use HashMap instead of Map.of() to handle null values
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
            log.error("Validation error in onboarding: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Internal server error in onboarding: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        try {
            authService.resendOtp(request.getEmail());
            return ResponseEntity.ok(Map.of("success", true, "message", "New OTP sent successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Resend OTP error", ex);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to resend OTP"));
        }
    }

    // Add a current user endpoint for debugging
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            String email = (String) auth.getPrincipal();
            User user = authService.getUserByEmail(email);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("name", user.getName());
            userInfo.put("username", user.getUsername());
            userInfo.put("onboardingCompleted", user.getOnboardingCompleted());

            return ResponseEntity.ok(Map.of("user", userInfo));
        } catch (Exception ex) {
            log.error("Get current user error", ex);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get user info"));
        }
    }
}