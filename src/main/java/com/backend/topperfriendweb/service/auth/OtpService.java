package com.backend.topperfriendweb.service.auth;

import com.backend.topperfriendweb.model.TempRegistration;
import com.backend.topperfriendweb.model.User;
import com.backend.topperfriendweb.repository.TempRegistrationRepository;
import com.backend.topperfriendweb.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final TempRegistrationRepository tempRegistrationRepository;
    private final UserRepository userRepository;

    public OtpService(TempRegistrationRepository tempRegistrationRepository,
                      UserRepository userRepository) {
        this.tempRegistrationRepository = tempRegistrationRepository;
        this.userRepository = userRepository;
    }

    public String generateOtp() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public Long verifyOtp(String email, String code) {
        String emailLower = email.trim().toLowerCase();

        TempRegistration tempReg = tempRegistrationRepository.findByEmailAndOtpCode(emailLower, code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        if (tempReg.getExpiresAt().isBefore(LocalDateTime.now())) {
            tempRegistrationRepository.deleteByEmail(emailLower);
            throw new IllegalArgumentException("OTP expired");
        }

        User user = new User();
        user.setUsername(tempReg.getUsername());
        user.setName(tempReg.getName());
        user.setEmail(tempReg.getEmail());
        user.setPassword(tempReg.getPassword());
        user.setEmailVerified(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        tempRegistrationRepository.deleteByEmail(emailLower);

        return savedUser.getId();
    }

    public static class ResendResult {
        private final String otp;
        private final String name;

        public ResendResult(String otp, String name) {
            this.otp = otp;
            this.name = name;
        }

        public String getOtp() { return otp; }
        public String getName() { return name; }
    }

    @Transactional
    public ResendResult resendOtp(String email) {
        TempRegistration tempReg = tempRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No registration in progress"));

        String newOtp = generateOtp();
        tempReg.setOtpCode(newOtp);
        tempReg.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        tempRegistrationRepository.save(tempReg);
        return new ResendResult(newOtp, tempReg.getName());
    }
}
