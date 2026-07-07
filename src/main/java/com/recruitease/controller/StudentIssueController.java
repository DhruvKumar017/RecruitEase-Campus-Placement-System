package com.recruitease.controller;

import java.util.List;

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

    public StudentIssueController(
            StudentIssueRepository studentIssueRepository
    ) {
        this.studentIssueRepository = studentIssueRepository;
    }

    /* Student submits issue */

    @PostMapping("/api/student-issues")
    public ResponseEntity<?> createStudentIssue(
            @RequestBody StudentIssue issue
    ) {
        if (issue.getStudentName() == null ||
            issue.getStudentName().trim().isEmpty() ||
            issue.getStudentEmail() == null ||
            issue.getStudentEmail().trim().isEmpty() ||
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

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedIssue);
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
}