package com.recruitease.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmailService {

    private static final String BREVO_URL =
            "https://api.brevo.com/v3/smtp/email";

    private static final String SENDER_EMAIL =
            "recruitease.campus.system@gmail.com";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String brevoApiKey;

    public EmailService(
            ObjectMapper objectMapper,
            @Value("${BREVO_API_KEY}") String brevoApiKey
    ) {
        this.objectMapper = objectMapper;
        this.brevoApiKey = brevoApiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendPasswordResetOtp(String email, String otp) {

        try {

            Map<String, Object> payload = Map.of(
                    "sender", Map.of(
                            "name", "RecruitEase",
                            "email", SENDER_EMAIL
                    ),
                    "to", List.of(
                            Map.of("email", email)
                    ),
                    "subject", "RecruitEase - Password Reset OTP",
                    "textContent",
                            "Hello,\n\n" +
                            "Your RecruitEase password reset OTP is: "
                            + otp +
                            "\n\nThis OTP is valid for 10 minutes." +
                            "\n\nDo not share this OTP with anyone." +
                            "\n\nRegards,\nRecruitEase Placement Cell"
            );

            String requestBody =
                    objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_URL))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", brevoApiKey)
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    requestBody
                            )
                    )
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Brevo API failed. HTTP "
                        + response.statusCode()
                        + " - "
                        + response.body()
                );
            }

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Brevo email request interrupted.",
                    exception
            );

        } catch (Exception exception) {

            System.err.println(
                    "===== RECRUITEASE BREVO MAIL ERROR ====="
            );

            exception.printStackTrace();

            throw new RuntimeException(
                    "Brevo email send failed.",
                    exception
            );
        }
    }
}