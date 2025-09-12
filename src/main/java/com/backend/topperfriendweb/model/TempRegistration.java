package com.backend.topperfriendweb.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// src/main/java/com/backend/topperfriendweb/model/TempRegistration.java
@Entity
@Table(name = "temp_registration", indexes = {
    @Index(columnList = "email", name = "idx_tempreg_email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    private String username;

    private String otpCode;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime expiresAt;

}