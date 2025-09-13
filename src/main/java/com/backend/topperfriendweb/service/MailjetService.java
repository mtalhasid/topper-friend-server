// src/main/java/com/backend/topperfriendweb/service/MailjetService.java
package com.backend.topperfriendweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class MailjetService {
    private final String apiKey;
    private final String apiSecret;
    private final String fromEmail;
    private final String fromName;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public MailjetService(
            @Value("${mailjet.apiKey:}") String apiKey,
            @Value("${mailjet.apiSecret:}") String apiSecret,
            @Value("${mailjet.fromEmail:trainedbot10k@gmail.com}") String fromEmail,
            @Value("${mailjet.fromName:Topper Friend}") String fromName) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public void sendVerificationEmail(String toEmail, String toName, String otp) throws Exception {
        String subject = "Your Verification Code";
        String text = String.format("Hello %s,\n\nYour verification code is: %s\n\nCode expires in 10 minutes.\n",
                toName, otp);
        String html = "<div style=\"font-family: Arial, sans-serif;max-width: 600px;margin: 0 auto;color: #333;\">" +
                "<h2>Your Verification Code</h2>" +
                "<p>Hello " + toName + ",</p>" +
                "<p>Use this code to verify your email:</p>" +
                "<div style=\"background: #f0f5ff;border: 2px dashed #4a90e2;padding: 20px;text-align: center;font-size: 28px;font-weight: bold;letter-spacing: 3px;margin: 25px 0;color: #1a365d;\">"
                +
                otp +
                "</div><p style=\"font-size: 14px; color: #666;\">Expires in 10 minutes • Do not share this code</p></div>";

        Map<String, Object> message = Map.of(
                "From", Map.of("Email", fromEmail, "Name", fromName),
                "To", new Map[] { Map.of("Email", toEmail, "Name", toName) },
                "Subject", subject,
                "TextPart", text,
                "HTMLPart", html);

        Map<String, Object> payload = Map.of("Messages", new Object[] { message });
        String body = mapper.writeValueAsString(payload);

        String auth = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mailjet.com/v3.1/send"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + auth)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) {
            throw new RuntimeException("Mailjet send failed: " + resp.statusCode() + " - " + resp.body());
        }
    }
}
