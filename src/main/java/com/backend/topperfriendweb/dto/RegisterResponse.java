// src/main/java/com/backend/topperfriendweb/dto/RegisterResponse.java
package com.backend.topperfriendweb.dto;

public class RegisterResponse {
    private String message;
    private boolean requiresVerification;
    private Long userId;

    public RegisterResponse(String message, boolean requiresVerification, Long userId) {
        this.message = message;
        this.requiresVerification = requiresVerification;
        this.userId = userId;
    }
    // getters
}
