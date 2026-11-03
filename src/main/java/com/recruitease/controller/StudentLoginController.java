package com.recruitease.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import com.recruitease.security.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JwtService jwtService;
private final PasswordEncoder passwordEncoder;

    public StudentLoginController(
            StudentLoginRepository studentLoginRepository,
            StudentRepository studentRepository,
PasswordResetOtpRepository passwordResetOtpRepository,
EmailService emailService,
JwtService jwtService,
PasswordEncoder passwordEncoder
    ) {
        this.studentLoginRepository = studentLoginRepository;
        this.studentRepository = studentRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.emailService = emailService;
this.jwtService = jwtService;
this.passwordEncoder = passwordEncoder;
    }

@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody StudentLogin loginRequest) {

StudentLogin studentLogin =
        studentLoginRepository.findByUsernameIgnoreCase(
                loginRequest.getUsername()
        );

if (studentLogin != null) {

    String storedPassword = studentLogin.getPassword();
    String enteredPassword = loginRequest.getPassword();

    boolean validPassword = false;

    if (storedPassword != null && enteredPassword != null) {

        if (storedPassword.matches("^\\$2[aby]\\$\\d{2}\\$.*")) {
            // Password already hashed using BCrypt
            validPassword = passwordEncoder.matches(
                    enteredPassword, storedPassword
            );
        } else {
            // Legacy plain-text password
            validPassword = storedPassword.equals(enteredPassword);

            if (validPassword) {
                studentLogin.setPassword(
                        passwordEncoder.encode(enteredPassword)
                );
                studentLoginRepository.save(studentLogin);
            }
        }
    }

    if (!validPassword) {
        studentLogin = null;
    }
}

    if (studentLogin == null) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Invalid username or password"));
    }

    Student student = studentRepository
            .findById(studentLogin.getStudentId())
            .orElse(null);

    if (student == null || student.isBlocked() || student.isDeleted()) {
        return ResponseEntity.status(403)
                .body(Map.of("message", "Student account is not active"));
    }

    String token = jwtService.generateStudentToken(student.getId());

    return ResponseEntity.ok(Map.of(
            "studentId", student.getId(),
            "username", studentLogin.getUsername(),
            "role", "STUDENT",
            "token", token
    ));
}

@PostMapping("/create")
public ResponseEntity<?> createLogin(@RequestBody StudentLogin studentLogin) {
    return ResponseEntity.status(403).body(
            Map.of("message", "Direct login creation is disabled.")
    );
}

@GetMapping("/by-student/{studentId}")
public ResponseEntity<?> getLoginByStudentId(
        @PathVariable Long studentId
) {
    Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
        return ResponseEntity.status(403).body(
                Map.of("message", "Authentication required.")
        );
    }

    boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a ->
                    a.getAuthority().equals("ROLE_MAIN_ADMIN") ||
                    a.getAuthority().equals("ROLE_ADMIN")
            );

    boolean isStudentOwner = auth.getAuthorities().stream()
            .anyMatch(a ->
                    a.getAuthority().equals("ROLE_STUDENT")
            ) && String.valueOf(studentId).equals(auth.getName());

    if (!isAdmin && !isStudentOwner) {
        return ResponseEntity.status(403).body(
                Map.of("message", "You are not authorized to update this username.")
        );
    }

    StudentLogin login =
            studentLoginRepository.findByStudentId(studentId);

    if (login == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(Map.of(
            "studentId", login.getStudentId(),
            "username", login.getUsername()
    ));
}
    /* ================= UPDATE USERNAME ================= */

    @PutMapping("/{studentId}/update-username")
    public ResponseEntity<?> updateUsername(
            @PathVariable Long studentId,
            @RequestBody StudentLogin request
    ) {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isStudentOwner =
                auth != null &&
                auth.isAuthenticated() &&
                auth.getAuthorities().stream()
                        .anyMatch(a ->
                                "ROLE_STUDENT".equals(a.getAuthority())
                        ) &&
                String.valueOf(studentId).equals(auth.getName());

        if (!isStudentOwner) {
            return ResponseEntity.status(403).body(
                    Map.of("message",
                            "Only the student can update their own username.")
            );
        }

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
    Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

    boolean isStudentOwner =
            auth != null &&
            auth.isAuthenticated() &&
            auth.getAuthorities().stream()
                .anyMatch(a ->
                    "ROLE_STUDENT".equals(a.getAuthority())
                ) &&
            String.valueOf(studentId).equals(auth.getName());

    if (!isStudentOwner) {
        return ResponseEntity.status(403).body(
                Map.of("message", "Only the student can change their own password.")
        );
    }

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

String storedPassword = login.getPassword();

boolean passwordMatches = storedPassword != null &&
        (storedPassword.matches("^\\$2[aby]\\$\\d{2}\\$.*")
                ? passwordEncoder.matches(oldPassword, storedPassword)
                : storedPassword.equals(oldPassword));

if (!passwordMatches) {
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

       login.setPassword(passwordEncoder.encode(newPassword));

        return ResponseEntity.ok(
                studentLoginRepository.save(login)
        );
    }

    /* ================= ADMIN RESET STUDENT PASSWORD ================= */

@PutMapping("/{studentId}/admin-reset-password")
public ResponseEntity<?> adminResetStudentPassword(
        @PathVariable Long studentId,
        @RequestBody Map<String, String> request
) {
    Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

    boolean isAdmin = auth != null
            && auth.isAuthenticated()
            && auth.getAuthorities().stream()
                .anyMatch(a ->
                    "ROLE_MAIN_ADMIN".equals(a.getAuthority())
                    || "ROLE_ADMIN".equals(a.getAuthority())
                );

    if (!isAdmin) {
        return ResponseEntity.status(403).body(
                Map.of("message", "Only Admin or Main Admin can reset student passwords.")
        );
    }

    StudentLogin login =
            studentLoginRepository.findByStudentId(studentId);

    if (login == null) {
        return ResponseEntity.status(404).body(
                Map.of("message", "Student login not found.")
        );
    }

    String newPassword = request == null
            ? null
            : request.get("newPassword");

    if (newPassword == null || newPassword.length() < 8) {
        return ResponseEntity.badRequest().body(
                Map.of("message", "Password must contain at least 8 characters.")
        );
    }

    login.setPassword(passwordEncoder.encode(newPassword));
    studentLoginRepository.save(login);

    return ResponseEntity.ok(
            Map.of("message", "Student password reset successfully.")
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

studentLogin.setPassword(
        passwordEncoder.encode(newPassword.trim())
);
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