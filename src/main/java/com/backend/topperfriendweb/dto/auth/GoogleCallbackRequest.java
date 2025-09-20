package com.backend.topperfriendweb.dto.auth;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCallbackRequest {
    @NotBlank
    private String code;

    // Optional OAuth state parameter; if provided, we will validate it to mitigate CSRF
    private String state;
}