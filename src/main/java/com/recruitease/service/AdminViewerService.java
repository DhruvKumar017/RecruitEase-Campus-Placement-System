
package com.recruitease.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitease.entity.AdminViewerCredential;
import com.recruitease.entity.AdminViewerEmailOtp;
import com.recruitease.repository.AdminViewerCredentialRepository;
import com.recruitease.repository.AdminViewerEmailOtpRepository;
import com.recruitease.security.JwtService;

@Service
public class AdminViewerService {

private final AdminViewerCredentialRepository repository;
private final AdminViewerEmailOtpRepository otpRepository;
private final PasswordEncoder passwordEncoder;
private final JwtService jwtService;

    private final SecureRandom secureRandom = new SecureRandom();

public AdminViewerService(
        AdminViewerCredentialRepository repository,
        AdminViewerEmailOtpRepository otpRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
) {
    this.repository = repository;
    this.otpRepository = otpRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
}

    // Generate temporary credentials for an authorized viewer.

@Transactional
public Map<String, String> generateCredentials(String email) {

    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException(
                "Verified email is required."
        );
    }

    String normalizedEmail =
            email.trim().toLowerCase(Locale.ROOT);

    AdminViewerEmailOtp otpRecord = otpRepository
            .findByEmailForUpdate(normalizedEmail)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Email verification required."
                    )
            );

    if (!otpRecord.isVerified()
            || otpRecord.isAccessUsed()) {
        throw new IllegalStateException(
                "Email is not verified or access already used."
        );
    }

    String viewerId;

    do {
        viewerId = "VIEW-" + randomString(12);
    } while (repository.existsByViewerId(viewerId));

    String password = randomString(24);

    AdminViewerCredential credential =
            new AdminViewerCredential();

    credential.setViewerId(viewerId);
    credential.setViewerEmail(normalizedEmail);
    credential.setPasswordHash(
            passwordEncoder.encode(password)
    );

    LocalDateTime now = LocalDateTime.now();

    credential.setCreatedAt(now);
    credential.setExpiresAt(now.plusMinutes(10));
    credential.setUsed(false);
    credential.setRevoked(false);

    repository.saveAndFlush(credential);

    otpRecord.setAccessUsed(true);
    otpRepository.save(otpRecord);

    Map<String, String> result = new HashMap<>();

    result.put("viewerId", viewerId);
    result.put("password", password);
    result.put("expiresIn", "10 minutes");

    return result;
}


    // Redeem the credentials once and issue a viewer-only JWT.
    @Transactional
    public String login(String viewerId, String password) {

        if (viewerId == null || password == null
                || viewerId.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid viewer credentials."
            );
        }

        AdminViewerCredential credential = repository
                .findByViewerId(viewerId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid viewer credentials."
                ));

        if (credential.isUsed()
                || credential.isRevoked()
                || !LocalDateTime.now().isBefore(
                        credential.getExpiresAt()
                )
                || !passwordEncoder.matches(
                        password,
                        credential.getPasswordHash()
                )) {

            throw new IllegalArgumentException(
                    "Invalid or expired viewer credentials."
            );
        }

credential.setUsed(true);

// Save successful login date and time
credential.setLoginAt(LocalDateTime.now());

repository.save(credential);

return jwtService.generateViewerToken(
        credential.getViewerId()
);
    }

    private String randomString(int byteCount) {

        byte[] bytes = new byte[byteCount];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
