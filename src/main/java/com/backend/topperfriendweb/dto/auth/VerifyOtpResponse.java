// OtpVerifyResponse.java
package com.backend.topperfriendweb.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponse {
    private String token;
    private Long userId;
}