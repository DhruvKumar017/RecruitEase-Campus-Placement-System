package com.recruitease.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.PasswordResetOtp;
import com.recruitease.entity.Student;
import com.recruitease.entity.StudentLogin;
import com.recruitease.repository.PasswordResetOtpRepository;
import com.recruitease.repository.StudentLoginRepository;
import com.recruitease.repository.StudentRepository;
import com.recruitease.service.EmailService;

@RestController
@RequestMapping("/api/student-login")
public class StudentLoginController {

    private final StudentLoginRepository studentLoginRepository;
    private final StudentRepository studentRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final EmailService emailService;

    public StudentLoginController(
            StudentLoginRepository studentLoginRepository,
            StudentRepository studentRepository,
            PasswordResetOtpRepository passwordResetOtpRepository,
            EmailService emailService
    ) {
        this.studentLoginRepository = studentLoginRepository;
        this.studentRepository = studentRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public StudentLogin login(@RequestBody StudentLogin loginRequest) {
        return studentLoginRepository.findByUsernameAndPassword(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
    }

    @PostMapping("/create")
    public StudentLogin createLogin(@RequestBody StudentLogin studentLogin) {
        return studentLoginRepository.save(studentLogin);
    }

    @GetMapping("/by-student/{studentId}")
    public StudentLogin getLoginByStudentId(
            @PathVariable Long studentId
    ) {
        return studentLoginRepository.findByStudentId(studentId);
    }

    /* ================= UPDATE USERNAME ================= */

    @PutMapping("/{studentId}/update-username")
    public ResponseEntity<?> updateUsername(
            @PathVariable Long studentId,
            @RequestBody StudentLogin request
    ) {
        StudentLogin login =
                studentLoginRepository.findByStudentId(studentId);

        if (login == null) {
            return ResponseEntity.notFound().build();
        }

        String newUsername = request.getUsername() == null
                ? ""
                : request.getUsername().trim();

        if (newUsername.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Username is required.")
            );
        }

        if (newUsername.length() < 3) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Username must contain at least 3 characters."
                    )
            );
        }

        StudentLogin existingLogin =
                studentLoginRepository.findByUsernameIgnoreCase(
                        newUsername
                );

        if (existingLogin != null &&
                !existingLogin.getStudentId().equals(studentId)) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "This username is already taken."
                    )
            );
        }

        login.setUsername(newUsername);

        return ResponseEntity.ok(
                studentLoginRepository.save(login)
        );
    }

    /* ================= CHANGE PASSWORD ================= */

    @PutMapping("/{studentId}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long studentId,
            @RequestBody StudentLogin request
    ) {
        StudentLogin login =
                studentLoginRepository.findByStudentId(studentId);

        if (login == null) {
            return ResponseEntity.notFound().build();
        }

        String oldPassword = request.getOldPassword() == null
                ? ""
                : request.getOldPassword().trim();

        String newPassword = request.getPassword() == null
                ? ""
                : request.getPassword().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Current password and new password are required."
                    )
            );
        }

        if (!login.getPassword().equals(oldPassword)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Current password is incorrect.")
            );
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "New password must contain at least 6 characters."
                    )
            );
        }

        login.setPassword(newPassword);

        return ResponseEntity.ok(
                studentLoginRepository.save(login)
        );
    }

    /* ================= FORGOT PASSWORD OTP ================= */

    @PostMapping("/forgot-password/send-otp")
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
                            "Username and registered email are required."
                    )
            );
        }

        StudentLogin studentLogin =
                studentLoginRepository.findByUsernameIgnoreCase(
                        username.trim()
                );

        if (studentLogin == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Username or registered email is incorrect."
                    )
            );
        }

        Student student = studentRepository
                .findById(studentLogin.getStudentId())
                .orElse(null);

        if (student == null ||
                student.isBlocked() ||
                student.isDeleted()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Student account is not active.")
            );
        }

        if (!student.getEmail().equalsIgnoreCase(email.trim())) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Username or registered email is incorrect."
                    )
            );
        }

        String otp = String.format(
                "%06d",
                new SecureRandom().nextInt(1_000_000)
        );

        PasswordResetOtp passwordResetOtp = new PasswordResetOtp();

        passwordResetOtp.setStudentId(student.getId());
        passwordResetOtp.setEmail(student.getEmail());
        passwordResetOtp.setOtp(otp);
        passwordResetOtp.setExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );
        passwordResetOtp.setVerified(false);

        passwordResetOtpRepository.save(passwordResetOtp);

        try {
            emailService.sendPasswordResetOtp(
                    student.getEmail(),
                    otp
            );

        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "OTP email could not be sent. Check mail setup."
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "OTP sent to your registered email."
                )
        );
    }

    /* ================= RESET PASSWORD ================= */

    @PostMapping("/forgot-password/reset")
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

            return ResponseEntity.badRequest().body(
                    Map.of("message", "All fields are required.")
            );
        }

        if (newPassword.trim().length() < 6) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "New password must contain at least 6 characters."
                    )
            );
        }

        StudentLogin studentLogin =
                studentLoginRepository.findByUsernameIgnoreCase(
                        username.trim()
                );

        if (studentLogin == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Username or registered email is incorrect."
                    )
            );
        }

        Student student = studentRepository
                .findById(studentLogin.getStudentId())
                .orElse(null);

        if (student == null ||
                student.isBlocked() ||
                student.isDeleted() ||
                !student.getEmail().equalsIgnoreCase(email.trim())) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Username or registered email is incorrect."
                    )
            );
        }

        PasswordResetOtp passwordResetOtp =
                passwordResetOtpRepository
                        .findTopByStudentIdAndEmailIgnoreCaseOrderByIdDesc(
                                student.getId(),
                                email.trim()
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
                    Map.of("message", "This OTP has already been used.")
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

        if (!passwordResetOtp.getOtp().equals(otp.trim())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Incorrect OTP.")
            );
        }

        studentLogin.setPassword(newPassword.trim());
        studentLoginRepository.save(studentLogin);

        passwordResetOtp.setVerified(true);
        passwordResetOtpRepository.save(passwordResetOtp);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset successfully. Please login with your new password."
                )
        );
    }
}