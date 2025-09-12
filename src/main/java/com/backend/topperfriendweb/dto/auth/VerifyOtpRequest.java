// src/main/java/com/backend/topperfriendweb/dto/VerifyOtpRequest.java
package com.backend.topperfriendweb.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String code;
}
