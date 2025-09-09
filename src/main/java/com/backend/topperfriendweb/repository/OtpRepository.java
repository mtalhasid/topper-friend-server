// src/main/java/com/backend/topperfriendweb/repository/OtpRepository.java
package com.backend.topperfriendweb.repository;

import com.backend.topperfriendweb.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndCodeAndExpiresAtAfter(String email, String code, LocalDateTime now);

    void deleteByEmailAndCode(String email, String code);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
