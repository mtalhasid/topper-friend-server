package com.backend.topperfriendweb.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {
    private String username;
    private String name;
    private String collegeName;
    private String rollNumber;
    private String image;
}
