package com.recruitease.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_issues")
public class StudentIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentName;

private String studentEmail;

// Login username entered in Student Help Center
private String loginUsername;

// Student registration ID
private String registrationId;

private String issueType;

    private String issueMessage;

private String status;

private LocalDateTime submittedAt;

// Admin reply details
@jakarta.persistence.Column(columnDefinition = "TEXT")
private String adminReply;

// Verified student account ID
// Null for complaints submitted without login
private Long studentId;

private LocalDateTime repliedAt;

    @PrePersist
    public void beforeSave() {
        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

public String getLoginUsername() {
    return loginUsername;
}

public void setLoginUsername(String loginUsername) {
    this.loginUsername = loginUsername;
}

public String getRegistrationId() {
    return registrationId;
}

public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
}

public String getIssueType() {
    return issueType;
}

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssueMessage() {
        return issueMessage;
    }

    public void setIssueMessage(String issueMessage) {
        this.issueMessage = issueMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

public String getAdminReply() {
    return adminReply;
}

public Long getStudentId() {
    return studentId;
}

public void setStudentId(Long studentId) {
    this.studentId = studentId;
}

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }
}