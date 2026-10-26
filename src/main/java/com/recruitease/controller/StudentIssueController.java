package com.recruitease.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.springframework.security.core.Authentication;

import com.recruitease.entity.Student;
import com.recruitease.entity.StudentLogin;

import com.recruitease.repository.StudentRepository;
import com.recruitease.repository.StudentLoginRepository;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.recruitease.entity.StudentIssue;
import com.recruitease.repository.StudentIssueRepository;

@RestController
public class StudentIssueController {

    private final StudentIssueRepository studentIssueRepository;

private final StudentRepository studentRepository;

private final StudentLoginRepository studentLoginRepository;

public StudentIssueController(
        StudentIssueRepository studentIssueRepository,
        StudentRepository studentRepository,
        StudentLoginRepository studentLoginRepository
) {
    this.studentIssueRepository = studentIssueRepository;
    this.studentRepository = studentRepository;
    this.studentLoginRepository = studentLoginRepository;
}

    /* Student views their own complaints and admin replies */

@GetMapping("/api/student-issues/my")
public ResponseEntity<?> getMyStudentIssues(
        Authentication authentication
) {

    if (authentication == null ||
        authentication.getAuthorities().stream()
            .noneMatch(authority ->
                authority.getAuthority().equals("ROLE_STUDENT")
            )) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Student login required.");
    }

    Long studentId;

    try {
        studentId = Long.parseLong(authentication.getName());
    } catch (NumberFormatException exception) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid student identity.");
    }

    Student student = studentRepository
            .findById(studentId)
            .orElse(null);

    if (student == null ||
        student.isBlocked() ||
        student.isDeleted()) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Student account is not active.");
    }

return ResponseEntity.ok(
        studentIssueRepository
            .findByStudentIdOrderBySubmittedAtDesc(
                studentId
            )
);
}

/* Student submits issue */

@PostMapping("/api/student-issues")
public ResponseEntity<?> createStudentIssue(
        @RequestBody StudentIssue issue,
        Authentication authentication
) {

// Never trust a studentId supplied in the request body.
issue.setStudentId(null);

// Validate account details for public Help Center complaints
boolean isLoggedInStudent = authentication != null
        && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority()));

if (!isLoggedInStudent) {

    String username = issue.getLoginUsername();
    String registrationId = issue.getRegistrationId();

    if (username == null || username.trim().isEmpty() ||
        registrationId == null || registrationId.trim().isEmpty()) {

        return ResponseEntity.badRequest()
                .body("Username and Registration ID are required.");
    }

    StudentLogin studentLogin = studentLoginRepository
            .findByUsernameIgnoreCase(username.trim());

    Student student = studentLogin == null
            ? null
            : studentRepository.findById(
                    studentLogin.getStudentId()
              ).orElse(null);

    if (student == null ||
        student.isBlocked() ||
        student.isDeleted() ||
        student.getRollNumber() == null ||
        !student.getRollNumber().trim()
                .equalsIgnoreCase(registrationId.trim())) {

        return ResponseEntity.badRequest()
                .body("Username or Registration ID is incorrect.");
    }

    issue.setLoginUsername(username.trim());
    issue.setRegistrationId(registrationId.trim());

    // Use the registered email, not the username as email
    issue.setStudentEmail(student.getEmail());
}

    if (authentication != null &&
        authentication.getAuthorities().stream()
            .anyMatch(authority ->
                "ROLE_STUDENT".equals(authority.getAuthority())
            )) {

        try {
            Long studentId = Long.parseLong(
                    authentication.getName()
            );

            Student student = studentRepository
                    .findById(studentId)
                    .orElse(null);

            if (student != null &&
                !student.isBlocked() &&
                !student.isDeleted()) {

                // Link only complaints submitted using
                // the authenticated student's own email.
                if (student.getEmail() != null &&
                    issue.getStudentEmail() != null &&
                    student.getEmail().trim().equalsIgnoreCase(
                        issue.getStudentEmail().trim()
                    )) {

                    issue.setStudentId(studentId);
                }
            }

        } catch (NumberFormatException exception) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid student identity.");
        }
    }
        if (issue.getStudentName() == null ||
            issue.getStudentName().trim().isEmpty() ||
            issue.getStudentEmail() == null ||
issue.getStudentEmail().trim().isEmpty() ||
(!isLoggedInStudent &&
    (issue.getLoginUsername() == null ||
     issue.getLoginUsername().trim().isEmpty() ||
     issue.getRegistrationId() == null ||
     issue.getRegistrationId().trim().isEmpty())) ||
issue.getIssueType() == null ||
            issue.getIssueType().trim().isEmpty() ||
            issue.getIssueMessage() == null ||
            issue.getIssueMessage().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Please fill all issue details.");
        }

        issue.setStudentName(issue.getStudentName().trim());
        issue.setStudentEmail(issue.getStudentEmail().trim());
        issue.setIssueType(issue.getIssueType().trim());
        issue.setIssueMessage(issue.getIssueMessage().trim());
        issue.setStatus("Pending");

StudentIssue savedIssue =
        studentIssueRepository.save(issue);

// Public Help Center: do not expose student details
if (!isLoggedInStudent) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Your issue has been submitted successfully.");
}

// Logged-in Student Support: preserve existing response
return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(savedIssue);
    }

    /* ================= VERIFIED COMPLAINT HISTORY ================= */

    @PostMapping("/api/student-issues/verify-history")
    public ResponseEntity<?> getVerifiedComplaintHistory(
            @RequestBody Map<String, String> request
    ) {

        String username = request.get("username");
        String registrationId = request.get("registrationId");
        String email = request.get("email");
        String password = request.get("password");

        if (username == null || username.isBlank() ||
            registrationId == null || registrationId.isBlank() ||
            email == null || email.isBlank() ||
            password == null || password.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("All four fields are required.");
        }

        StudentLogin studentLogin = studentLoginRepository
                .findByUsernameAndPassword(
                        username.trim(),
                        password
                );

        if (studentLogin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid account credentials.");
        }

        Student student = studentRepository
                .findById(studentLogin.getStudentId())
                .orElse(null);

        if (student == null ||
            student.isBlocked() ||
            student.isDeleted()) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Student account is not active.");
        }

        if (student.getRollNumber() == null ||
            student.getEmail() == null ||
            !student.getRollNumber().trim()
                    .equalsIgnoreCase(registrationId.trim()) ||
            !student.getEmail().trim()
                    .equalsIgnoreCase(email.trim())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid account credentials.");
        }

        // Combine logged-in and public Help Center complaints.
        List<StudentIssue> complaints = new ArrayList<>();

        complaints.addAll(
                studentIssueRepository
                        .findByStudentIdOrderBySubmittedAtDesc(
                                student.getId()
                        )
        );

        complaints.addAll(
                studentIssueRepository
                        .findByStudentEmailIgnoreCaseOrderBySubmittedAtDesc(
                                student.getEmail()
                        )
        );

        // Remove duplicates by complaint ID.
        Map<Long, StudentIssue> uniqueComplaints = new LinkedHashMap<>();

        for (StudentIssue issue : complaints) {

            if (issue.getStudentId() != null &&
                !issue.getStudentId().equals(student.getId())) {
                continue;
            }

            uniqueComplaints.put(issue.getId(), issue);
        }

        List<StudentIssue> sortedComplaints =
                new ArrayList<>(uniqueComplaints.values());

        sortedComplaints.sort(
                Comparator.comparing(
                        StudentIssue::getSubmittedAt,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        // Return only complaint information, not student credentials.
        List<Map<String, Object>> result = new ArrayList<>();

        for (StudentIssue issue : sortedComplaints) {

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("id", issue.getId());
            item.put("issueType", issue.getIssueType());
            item.put("issueMessage", issue.getIssueMessage());
            item.put("status", issue.getStatus());
            item.put("adminReply", issue.getAdminReply());
            item.put("submittedAt", issue.getSubmittedAt());

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    /* Admin sees all student issues */

    @GetMapping("/api/student-issues")
    public List<StudentIssue> getAllStudentIssues() {
        return studentIssueRepository.findAll();
    }

    /* Admin sees only pending student issues */

    @GetMapping("/api/student-issues/pending")
    public List<StudentIssue> getPendingStudentIssues() {
        return studentIssueRepository
                .findByStatusIgnoreCase("Pending");
    }

    /* Admin updates issue status */

    @PutMapping("/api/student-issues/{id}/status/{status}")
    public ResponseEntity<?> updateIssueStatus(
            @PathVariable Long id,
            @PathVariable String status
    ) {
        StudentIssue issue = studentIssueRepository
                .findById(id)
                .orElse(null);

        if (issue == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Student issue not found.");
        }

        String validStatus = status.trim();

        if (!validStatus.equalsIgnoreCase("Pending") &&
            !validStatus.equalsIgnoreCase("In Progress") &&
            !validStatus.equalsIgnoreCase("Resolved")) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid status. Use Pending, In Progress or Resolved.");
        }

        issue.setStatus(validStatus);

        return ResponseEntity.ok(
                studentIssueRepository.save(issue)
        );
    }

    /* ================= ADMIN REPLY TO STUDENT ISSUE ================= */

    @PutMapping("/api/student-issues/{id}/reply")
    public ResponseEntity<?> replyToStudentIssue(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {

        StudentIssue issue = studentIssueRepository
                .findById(id)
                .orElse(null);

        if (issue == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Student issue not found.");
        }

        String adminReply = request.get("adminReply");

        if (adminReply == null || adminReply.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Admin reply cannot be empty.");
        }

        issue.setAdminReply(adminReply.trim());
        issue.setRepliedAt(LocalDateTime.now());
        issue.setStatus("Resolved");

        StudentIssue savedIssue =
                studentIssueRepository.save(issue);

        return ResponseEntity.ok(savedIssue);
    }
}