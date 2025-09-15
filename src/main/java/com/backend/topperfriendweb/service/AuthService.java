package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.auth.LoginResponse;
import com.backend.topperfriendweb.dto.auth.OnboardingRequest;
import com.backend.topperfriendweb.dto.auth.RegisterRequest;
import com.backend.topperfriendweb.model.TempRegistration;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.TempRegistrationRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TempRegistrationRepository tempRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailjetService mailjetService;
    private final JwtUtil jwtUtil;

    private static final int OTP_EXPIRY_MINUTES = 10;

    public AuthService(UserRepository userRepository,
                       TempRegistrationRepository tempRegistrationRepository,
                       PasswordEncoder passwordEncoder,
                       MailjetService mailjetService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tempRegistrationRepository = tempRegistrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailjetService = mailjetService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String register(RegisterRequest req) {
        String emailLower = req.getEmail().trim().toLowerCase();

        // Check if user already exists
        if (userRepository.existsByEmailIgnoreCase(emailLower)) {
            throw new IllegalArgumentException("User already exists");
        }

        // Check if temporary registration already exists
        if (tempRegistrationRepository.existsByEmail(emailLower)) {
            throw new IllegalArgumentException("Registration already in progress");
        }

        // Store in TEMPORARY registration (NOT in users table)
        TempRegistration tempReg = new TempRegistration();
        tempReg.setName(req.getName());
        tempReg.setEmail(emailLower);
        tempReg.setPassword(passwordEncoder.encode(req.getPassword()));
        tempReg.setUsername(req.getUsername());

        // Generate and store OTP
        String otp = generateOtp();
        tempReg.setOtpCode(otp);
        tempReg.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));

        tempRegistrationRepository.save(tempReg);

        // Send OTP email
        try {
            mailjetService.sendVerificationEmail(emailLower, req.getName(), otp);
            return "OTP sent to email";
        } catch (Exception e) {
            // Delete temp registration if email fails
            tempRegistrationRepository.deleteByEmail(emailLower);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    // FIXED: Updated login method to return enhanced response
    @Transactional(readOnly = true)
    public Map<String, Object> loginEnhanced(String email, String password) {
        String emailLower = email.trim().toLowerCase();
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(emailLower);

        Map<String, Object> response = new HashMap<>();

        if (optionalUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        User user = optionalUser.get();

        if (user.getEmailVerified() == null) {
            response.put("success", false);
            response.put("message", "Email not verified");
            return response;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "Incorrect password");
            return response;
        }

        // Generate token with complete user info
        String token = jwtUtil.generateTokenWithUserInfo(user.getId(), user.getEmail(), user.getName());

        response.put("success", true);
        response.put("message", "Login successful");
        response.put("token", token);

        // Add user info to response
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("username", user.getUsername());
        userInfo.put("onboardingCompleted", user.getOnboardingCompleted());

        response.put("user", userInfo);

        return response;
    }

    // Keep original method for backward compatibility
    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        Map<String, Object> enhancedResponse = loginEnhanced(email, password);
        return new LoginResponse(
                (Boolean) enhancedResponse.get("success"),
                (String) enhancedResponse.get("message"),
                (String) enhancedResponse.get("token")
        );
    }

    @Transactional
    public User completeOnboarding(Long userId, OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Username and name are required");
        }

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(request.getUsername().trim(), userId)) {
            throw new IllegalArgumentException("Username already taken");
        }

        user.setUsername(request.getUsername().trim());
        user.setName(request.getName().trim());
        user.setCollegeName(request.getCollegeName() != null ? request.getCollegeName().trim() : null);
        user.setRollNumber(request.getRollNumber() != null ? request.getRollNumber().trim() : null);
        user.setOnboardingCompleted(true);

        if (request.getImage() != null && request.getImage().contains("cloudinary.com")) {
            user.setImage(request.getImage());
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String generateOtp() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public Long verifyOtp(String email, String code) {
        String emailLower = email.trim().toLowerCase();

        // Find temporary registration
        TempRegistration tempReg = tempRegistrationRepository.findByEmailAndOtpCode(emailLower, code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        // Check if OTP is expired
        if (tempReg.getExpiresAt().isBefore(LocalDateTime.now())) {
            tempRegistrationRepository.deleteByEmail(emailLower);
            throw new IllegalArgumentException("OTP expired");
        }

        // NOW create the actual user
        User user = new User();
        user.setUsername(tempReg.getUsername());
        user.setName(tempReg.getName());
        user.setEmail(tempReg.getEmail());
        user.setPassword(tempReg.getPassword());
        user.setEmailVerified(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Delete temporary registration
        tempRegistrationRepository.deleteByEmail(emailLower);

        return savedUser.getId();
    }

    @Transactional
    public void resendOtp(String email) throws Exception {
        // Check if temporary registration exists
        TempRegistration tempReg = tempRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No registration in progress"));

        // Generate new OTP
        String newOtp = generateOtp();
        tempReg.setOtpCode(newOtp);
        tempReg.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));

        tempRegistrationRepository.save(tempReg);

        // Send email
        mailjetService.sendVerificationEmail(email, tempReg.getName(), newOtp);
    }
}