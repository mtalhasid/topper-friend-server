// src/main/java/com/backend/topperfriendweb/service/AuthService.java
package com.backend.topperfriendweb.service;

import com.backend.topperfriendweb.dto.LoginResponse;
import com.backend.topperfriendweb.dto.OnboardingRequest;
import com.backend.topperfriendweb.dto.RegisterRequest;
import com.backend.topperfriendweb.model.Otp;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.OtpRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import com.backend.topperfriendweb.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailjetService mailjetService;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final JwtUtil jwtUtil; // ⚠ make sure the name matches

    private static final int OTP_EXPIRY_MINUTES = 10;

    public AuthService(UserRepository userRepository, OtpRepository otpRepository,
            PasswordEncoder passwordEncoder, MailjetService mailjetService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailjetService = mailjetService;
        this.jwtUtil = jwtUtil; // use camelCase here
    }

    @Transactional
    public Long register(RegisterRequest req) {
        String emailLower = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(emailLower)) {
            throw new IllegalArgumentException("User already exists");
        }
        if (req.getUsername() != null && userRepository.existsByUsernameIgnoreCase(req.getUsername().trim())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = new User();
        user.setUsername(req.getUsername() != null ? req.getUsername().trim() : null);
        user.setName(req.getName().trim());
        user.setEmail(emailLower);
        user.setPassword(passwordEncoder.encode(req.getPassword().trim()));
        User saved = userRepository.save(user);

        // generate OTP
        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        Otp otpEntity = new Otp();
        otpEntity.setEmail(emailLower);
        otpEntity.setCode(otp);
        otpEntity.setExpiresAt(expiresAt);
        otpRepository.save(otpEntity);

        try {
            mailjetService.sendVerificationEmail(emailLower, user.getName() != null ? user.getName() : emailLower, otp);
        } catch (Exception e) {
            logger.error("Email sending failed", e);
            // continue — user created, OTP stored
        }

        return saved.getId();
    }

    private String generateOtp() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        String emailLower = email.trim().toLowerCase();
        var optional = otpRepository.findByEmailAndCodeAndExpiresAtAfter(emailLower, code, LocalDateTime.now());
        if (optional.isEmpty()) {
            return false;
        }
        // mark user verified
        userRepository.findByEmailIgnoreCase(emailLower).ifPresent(user -> {
            user.setEmailVerified(LocalDateTime.now());
            userRepository.save(user);
        });
        // delete otp
        otpRepository.deleteByEmailAndCode(emailLower, code);
        return true;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        String emailLower = email.trim().toLowerCase();
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(emailLower);

        if (optionalUser.isEmpty()) {
            return new LoginResponse(false, "User not found");
        }

        User user = optionalUser.get();

        if (user.getEmailVerified() == null) {
            return new LoginResponse(false, "Email not verified");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new LoginResponse(false, "Incorrect password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(true, "Login successful", token);
    }

    // src/main/java/com/backend/topperfriendweb/service/AuthService.java
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

    @Transactional
    public void resendOtp(String email) throws Exception {
        // Check if user exists
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate OTP
        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        // Delete old OTPs
        otpRepository.deleteByEmailAndCode(email, otpCode); // optional, or delete all by email
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now()); // cleanup old expired OTPs

        // Save new OTP
        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setCode(otpCode);
        otp.setExpiresAt(expiresAt);
        otpRepository.save(otp);

        // Send email
        mailjetService.sendVerificationEmail(email, user.getName(), otpCode);
    }

}
