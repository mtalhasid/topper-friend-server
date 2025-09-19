package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.auth.*;
import com.backend.topperfriendweb.dto.note.NoteDTO;
import com.backend.topperfriendweb.dto.userprofile.UserProfileDTO;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.service.AuthService;
import com.backend.topperfriendweb.service.NoteService;
import com.backend.topperfriendweb.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final NoteService noteService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest req) {
        String message = authService.register(req);
        RegisterResponse registerData = new RegisterResponse(message, true, null);
        return ResponseEntity.ok(CommonResponse.success(message, registerData));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CommonResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        Long userId = authService.verifyOtp(req.getEmail(), req.getCode());
        User user = authService.getUserByEmail(req.getEmail());
        String token = jwtUtil.generateTokenWithUserInfo(user.getId(), user.getEmail(), user.getName());

        VerifyOtpResponse otpData = new VerifyOtpResponse(token, userId);
        return ResponseEntity.ok(CommonResponse.success("Email verified successfully", otpData));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        LoginResponse loginData = authService.login(request.getEmail(), request.getPassword());

        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(CommonResponse.success("Login successful", loginData));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = (String) auth.getPrincipal();
            log.info("User {} logged out successfully", email);
        }

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(CommonResponse.success("Logged out successfully"));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<CommonResponse<UserProfileDTO>> onboarding(@Valid @RequestBody OnboardingRequest request) {
        log.info("Received onboarding request");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();

        User user = authService.getUserByEmail(email);
        User updatedUser = authService.completeOnboarding(user.getId(), request);

        UserProfileDTO userProfile = createUserProfileDTO(updatedUser);
        return ResponseEntity.ok(CommonResponse.success("Onboarding completed successfully", userProfile));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<CommonResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok(CommonResponse.success("New OTP sent successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserProfileDTO>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Not authenticated");
        }

        String email = (String) auth.getPrincipal();
        User user = authService.getUserByEmail(email);

        UserProfileDTO userProfile = createUserProfileDTO(user);
        return ResponseEntity.ok(CommonResponse.success("User info retrieved successfully", userProfile));
    }

    @PostMapping("/google-callback")
    public ResponseEntity<CommonResponse<LoginResponse>> googleCallback(@Valid @RequestBody GoogleCallbackRequest request) {
        log.info("Google callback with authorization code");

        LoginResponse loginData = authService.handleGoogleCallback(request.getCode());
        return ResponseEntity.ok(CommonResponse.success("Google login successful", loginData));
    }

    private UserProfileDTO createUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setCollegeName(user.getCollegeName());
        dto.setRollNumber(user.getRollNumber());
        dto.setImage(user.getImage());
        dto.setOnboardingCompleted(user.getOnboardingCompleted());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Add notes data - let GlobalExceptionHandler handle any errors
        List<NoteDTO> userNotes = noteService.getUserNotes(user.getId());
        dto.setNotes(userNotes);
        dto.setTotalNotes(userNotes.size());
        dto.setTotalLikes(userNotes.stream().mapToInt(NoteDTO::getLikes).sum());

        return dto;
    }
}