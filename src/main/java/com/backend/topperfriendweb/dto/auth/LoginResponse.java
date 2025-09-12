package com.backend.topperfriendweb.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.token = null;
    }

    public LoginResponse(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }

    // getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
