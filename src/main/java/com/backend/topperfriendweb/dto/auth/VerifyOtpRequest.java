// src/main/java/com/backend/topperfriendweb/dto/VerifyOtpRequest.java
package com.backend.topperfriendweb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyOtpRequest {
    @NotBlank
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @NotBlank
    private String code;

    // getters & setters
}
