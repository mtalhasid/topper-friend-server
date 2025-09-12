package com.backend.topperfriendweb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponse {
    private String message;
    private boolean requiresVerification;
    private Long userId;

    // ADD THIS DEFAULT CONSTRUCTOR
    public RegisterResponse() {}

    public RegisterResponse(String message, boolean requiresVerification, Long userId) {
        this.message = message;
        this.requiresVerification = requiresVerification;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRequiresVerification() {
        return requiresVerification;
    }

    public void setRequiresVerification(boolean requiresVerification) {
        this.requiresVerification = requiresVerification;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}