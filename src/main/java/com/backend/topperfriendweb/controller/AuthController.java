package com.backend.topperfriendweb.controller;

import com.backend.topperfriendweb.dto.CommonResponse;
import com.backend.topperfriendweb.dto.auth.*;
import com.backend.topperfriendweb.dto.userprofile.UserProfileDTO;
import com.backend.topperfriendweb.mapper.UserMapper;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.service.auth.AuthService;
import com.backend.topperfriendweb.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/auth")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;


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

        UserProfileDTO userProfile = userMapper.toUserProfileDTO(updatedUser);
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

        UserProfileDTO userProfile = userMapper.toUserProfileDTO(user);
        return ResponseEntity.ok(CommonResponse.success("User info retrieved successfully", userProfile));
    }

    @PostMapping("/google-callback")
    public ResponseEntity<CommonResponse<LoginResponse>> googleCallback(@Valid @RequestBody GoogleCallbackRequest request) {
        log.info("Google callback with authorization code");

        LoginResponse loginData = authService.handleGoogleCallback(request.getCode(), request.getState());
        return ResponseEntity.ok(CommonResponse.success("Google login successful", loginData));
    }
}