
package com.recruitease.service;

import com.recruitease.entity.AdminViewerEmailOtp;
import com.recruitease.repository.AdminViewerEmailOtpRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminViewerOtpService {

    private final AdminViewerEmailOtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public AdminViewerOtpService(
            AdminViewerEmailOtpRepository otpRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
        @Transactional
    public java.util.Map<String, String> verifyAndGenerateCredentials(
            String email,
            String otp,
            AdminViewerService viewerService
    ) {
if (!verifyOtp(email, otp)) {
    return null;
}

        return viewerService.generateCredentials(email);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email is required.");
        }

        String normalized =
                email.trim().toLowerCase(Locale.ROOT);

        if (!normalized.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        return normalized;
    }

    @Transactional
    public void sendOtp(String email) {

        String normalizedEmail = normalizeEmail(email);

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

AdminViewerEmailOtp record = otpRepository
        .findByEmailForUpdate(normalizedEmail)
        .orElseGet(AdminViewerEmailOtp::new);

LocalDateTime now = LocalDateTime.now();

// Prevent repeated OTP requests within 60 seconds
if (record.getId() != null
        && record.getCreatedAt() != null
        && now.isBefore(record.getCreatedAt().plusSeconds(60))) {

    throw new IllegalStateException(
            "Please wait 60 seconds before requesting another OTP."
    );
}

// Update last OTP request time
record.setCreatedAt(now);

        record.setEmail(normalizedEmail);
        record.setOtpHash(passwordEncoder.encode(otp));
        record.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );
        record.setAttempts(0);
        record.setVerified(false);
        record.setAccessUsed(false);

        otpRepository.saveAndFlush(record);

        emailService.sendViewerVerificationOtp(
                normalizedEmail,
                otp
        );
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {

        String normalizedEmail = normalizeEmail(email);

        AdminViewerEmailOtp record =
                otpRepository.findByEmailForUpdate(normalizedEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid verification request."
                                )
                        );

        if (record.isAccessUsed() || record.isVerified()) {
            return false;
        }

        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            return false;
        }

        if (record.getAttempts() >= 5) {
            return false;
        }

        record.setAttempts(record.getAttempts() + 1);

        if (otp == null ||
                !otp.matches("\\d{6}") ||
                !passwordEncoder.matches(otp, record.getOtpHash())) {

            otpRepository.save(record);
            return false;
        }

        record.setVerified(true);
        otpRepository.save(record);

        return true;
    }
}
