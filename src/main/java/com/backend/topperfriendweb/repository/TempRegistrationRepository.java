package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.TempRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// src/main/java/com/backend/topperfriendweb/repository/TempRegistrationRepository.java
public interface TempRegistrationRepository extends JpaRepository<TempRegistration, Long> {
    Optional<TempRegistration> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
    Optional<TempRegistration> findByEmailAndOtpCode(String email, String otpCode);
}