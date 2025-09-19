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
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {
    @Value("${google.oauth.client-id}")
    private String googleClientId;

    @Value("${google.oauth.client-secret}")
    private String googleClientSecret;

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

    // Clean login method that returns LoginResponse
    public LoginResponse login(String email, String password) {
        String emailLower = email.trim().toLowerCase();
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(emailLower);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = optionalUser.get();

        if (user.getEmailVerified() == null) {
            throw new IllegalArgumentException("Email not verified");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        String token = jwtUtil.generateTokenWithUserInfo(user.getId(), user.getEmail(), user.getName());
        return new LoginResponse(true, "Login successful", token);
    }

    // Keep the old method for backward compatibility if needed elsewhere
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

        String token = jwtUtil.generateTokenWithUserInfo(user.getId(), user.getEmail(), user.getName());

        response.put("success", true);
        response.put("message", "Login successful");
        response.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("username", user.getUsername());
        userInfo.put("onboardingCompleted", user.getOnboardingCompleted());

        response.put("user", userInfo);
        return response;
    }

    // Clean Google callback method that returns LoginResponse
    public LoginResponse handleGoogleCallback(String authorizationCode) {
        try {
            // Exchange authorization code for access token
            Map<String, Object> tokenData = exchangeCodeForTokens(authorizationCode);
            String accessToken = (String) tokenData.get("access_token");

            // Get user info from Google
            Map<String, Object> userInfo = getUserInfoFromGoogle(accessToken);

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");

            // Check if user exists
            User existingUser = null;
            try {
                existingUser = getUserByEmail(email);
            } catch (IllegalArgumentException e) {
                // User doesn't exist
            }

            if (existingUser != null) {
                // User exists - log them in
                String token = jwtUtil.generateTokenWithUserInfo(existingUser.getId(), existingUser.getEmail(), existingUser.getName());
                return new LoginResponse(true, "Login successful", token);
            } else {
                // Create new user
                User newUser = User.builder()
                        .name(name)
                        .email(email.toLowerCase())
                        .password(passwordEncoder.encode("GOOGLE_AUTH_" + System.currentTimeMillis()))
                        .username(email.split("@")[0])
                        .emailVerified(LocalDateTime.now())
                        .image(picture)
                        .onboardingCompleted(false)
                        .build();

                User savedUser = userRepository.save(newUser);
                String token = jwtUtil.generateTokenWithUserInfo(savedUser.getId(), savedUser.getEmail(), savedUser.getName());
                return new LoginResponse(true, "Account created successfully", token);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process Google callback: " + e.getMessage());
        }
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
    public void resendOtp(String email) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to resend OTP: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> exchangeCodeForTokens(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", googleClientId);
        params.put("client_secret", googleClientSecret);
        params.put("redirect_uri", "http://localhost:3000/auth/google/callback");
        params.put("grant_type", "authorization_code");

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(tokenUrl, params, Map.class);
    }

    private Map<String, Object> getUserInfoFromGoogle(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(userInfoUrl, Map.class);
    }
}