package com.recruitease.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.AdminPasswordResetOtp;
import com.recruitease.entity.AdminRequest;
import com.recruitease.entity.MainAdminCredential;
import com.recruitease.repository.AdminPasswordResetOtpRepository;
import com.recruitease.repository.AdminRequestRepository;
import com.recruitease.repository.MainAdminCredentialRepository;
import com.recruitease.security.JwtService;
import com.recruitease.service.EmailService;

@RestController
public class AdminLoginController {

    private final AdminRequestRepository adminRequestRepository;
    private final AdminPasswordResetOtpRepository adminPasswordResetOtpRepository;
    private final MainAdminCredentialRepository mainAdminCredentialRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Value("${app.main-admin.username}")
    private String mainAdminUsername;

    @Value("${app.main-admin.password}")
    private String mainAdminPassword;

    @Value("${app.main-admin.email}")
    private String mainAdminEmail;

    public AdminLoginController(
            AdminRequestRepository adminRequestRepository,
            AdminPasswordResetOtpRepository adminPasswordResetOtpRepository,
            MainAdminCredentialRepository mainAdminCredentialRepository,
            EmailService emailService,
            JwtService jwtService
    ) {
        this.adminRequestRepository = adminRequestRepository;
        this.adminPasswordResetOtpRepository = adminPasswordResetOtpRepository;
        this.mainAdminCredentialRepository = mainAdminCredentialRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    /* ================= ADMIN LOGIN + JWT TOKEN ================= */

    @PostMapping("/api/admin-login/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginData
    ) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null ||
                username.trim().isEmpty() ||
                password.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Username and password are required.");
        }

        username = username.trim();
        password = password.trim();

        /* Main Admin from database */

        MainAdminCredential mainAdmin =
                mainAdminCredentialRepository
                        .findTopByUsernameIgnoreCase(username)
                        .orElse(null);

        if (mainAdmin != null &&
                password.equals(mainAdmin.getPassword())) {

            return ResponseEntity.ok(
                    createLoginResponse(
                            mainAdmin.getUsername(),
                            "MAIN_ADMIN"
                    )
            );
        }

        /* Main Admin from application.properties */

        if (username.equalsIgnoreCase(mainAdminUsername) &&
                password.equals(mainAdminPassword)) {

            return ResponseEntity.ok(
                    createLoginResponse(
                            mainAdminUsername,
                            "MAIN_ADMIN"
                    )
            );
        }

        /* Approved Admin */

        AdminRequest request = adminRequestRepository
                .findByRequestedUsernameIgnoreCase(username)
                .orElse(null);

        if (request == null) {
            return ResponseEntity.status(401)
                    .body(
                            "Admin account not found. Request access from Main Admin."
                    );
        }

        if (!"Approved".equalsIgnoreCase(request.getStatus())) {
            return ResponseEntity.status(403)
                    .body(
                            "Your admin request is still " +
                            request.getStatus() + "."
                    );
        }

        if (!password.equals(request.getRequestedPassword())) {
            return ResponseEntity.status(401)
                    .body("Incorrect admin password.");
        }

        return ResponseEntity.ok(
                createLoginResponse(
                        request.getRequestedUsername(),
                        "ADMIN"
                )
        );
    }

    /* ================= FORGOT PASSWORD OTP ================= */

    @PostMapping("/api/admin-login/forgot-password/send-otp")
    public ResponseEntity<?> sendForgotPasswordOtp(
            @RequestBody Map<String, String> request
    ) {
        String username = request.get("username");
        String email = request.get("email");

        if (username == null || email == null ||
                username.trim().isEmpty() ||
                email.trim().isEmpty()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Admin username and registered email are required."
                    )
            );
        }

        username = username.trim();
        email = email.trim();

        boolean isMainAdmin = false;

        MainAdminCredential mainAdmin =
                mainAdminCredentialRepository
                        .findTopByUsernameIgnoreCaseAndEmailIgnoreCase(
                                username,
                                email
                        )
                        .orElse(null);

        if (mainAdmin != null) {
            isMainAdmin = true;

        } else if (username.equalsIgnoreCase(mainAdminUsername) &&
                email.equalsIgnoreCase(mainAdminEmail)) {

            isMainAdmin = true;

            MainAdminCredential newMainAdmin =
                    new MainAdminCredential();

            newMainAdmin.setUsername(mainAdminUsername);
            newMainAdmin.setEmail(mainAdminEmail);
            newMainAdmin.setPassword(mainAdminPassword);

            mainAdminCredentialRepository.save(newMainAdmin);
        }

        AdminRequest approvedAdmin = null;

        if (!isMainAdmin) {
            approvedAdmin = adminRequestRepository
                    .findByRequestedUsernameIgnoreCaseAndEmailIgnoreCase(
                            username,
                            email
                    )
                    .orElse(null);

            if (approvedAdmin == null ||
                    !"Approved".equalsIgnoreCase(
                            approvedAdmin.getStatus()
                    )) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Username or registered email is incorrect."
                        )
                );
            }
        }

        String otp = String.format(
                "%06d",
                new SecureRandom().nextInt(1_000_000)
        );

        AdminPasswordResetOtp passwordResetOtp =
                new AdminPasswordResetOtp();

        passwordResetOtp.setUsername(username);
        passwordResetOtp.setEmail(email);
        passwordResetOtp.setOtp(otp);
        passwordResetOtp.setExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );
        passwordResetOtp.setVerified(false);

        adminPasswordResetOtpRepository.save(passwordResetOtp);

        try {
            emailService.sendPasswordResetOtp(email, otp);

        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "OTP email could not be sent. Check mail configuration."
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "OTP sent successfully to your registered email."
                )
        );
    }

    /* ================= RESET PASSWORD ================= */

    @PostMapping("/api/admin-login/forgot-password/reset")
    public ResponseEntity<?> resetForgotPassword(
            @RequestBody Map<String, String> request
    ) {
        String username = request.get("username");
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (username == null || email == null ||
                otp == null || newPassword == null ||
                username.trim().isEmpty() ||
                email.trim().isEmpty() ||
                otp.trim().isEmpty() ||
                newPassword.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "All fields are required."
                            )
                    );
        }

        username = username.trim();
        email = email.trim();
        otp = otp.trim();
        newPassword = newPassword.trim();

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "New password must contain at least 6 characters."
                    )
            );
        }

        AdminPasswordResetOtp passwordResetOtp =
                adminPasswordResetOtpRepository
                        .findTopByUsernameIgnoreCaseAndEmailIgnoreCaseOrderByIdDesc(
                                username,
                                email
                        )
                        .orElse(null);

        if (passwordResetOtp == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Please request a new OTP first."
                    )
            );
        }

        if (passwordResetOtp.isVerified()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "This OTP has already been used."
                    )
            );
        }

        if (LocalDateTime.now().isAfter(
                passwordResetOtp.getExpiresAt()
        )) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "OTP expired. Please request a new OTP."
                    )
            );
        }

        if (!passwordResetOtp.getOtp().equals(otp)) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Incorrect OTP."
                    )
            );
        }

        MainAdminCredential mainAdmin =
                mainAdminCredentialRepository
                        .findTopByUsernameIgnoreCaseAndEmailIgnoreCase(
                                username,
                                email
                        )
                        .orElse(null);

        if (mainAdmin != null) {
            mainAdmin.setPassword(newPassword);
            mainAdminCredentialRepository.save(mainAdmin);

        } else {
            AdminRequest approvedAdmin =
                    adminRequestRepository
                            .findByRequestedUsernameIgnoreCaseAndEmailIgnoreCase(
                                    username,
                                    email
                            )
                            .orElse(null);

            if (approvedAdmin == null ||
                    !"Approved".equalsIgnoreCase(
                            approvedAdmin.getStatus()
                    )) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Admin account not found or not approved."
                        )
                );
            }

            approvedAdmin.setRequestedPassword(newPassword);
            adminRequestRepository.save(approvedAdmin);
        }

        passwordResetOtp.setVerified(true);
        adminPasswordResetOtpRepository.save(passwordResetOtp);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset successfully. Please login with your new password."
                )
        );
    }

    /* ================= JWT LOGIN RESPONSE ================= */

    private Map<String, String> createLoginResponse(
            String username,
            String role
    ) {
        Map<String, String> result = new HashMap<>();

        result.put("username", username);
        result.put("role", role);

        result.put(
                "token",
                jwtService.generateToken(username, role)
        );

        return result;
    }
}