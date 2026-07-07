package com.recruitease.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "interview_requests")
public class InterviewRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;
    private String studentEmail;

    private Long companyId;
    private String companyName;

    private Long applicationId;
    private Long testId;

    private Double aptitudePercentage;

    private String interviewStatus;

    private String interviewDate;
    private String interviewTime;
    private String interviewMode;
    private String interviewLink;
    private String adminRemark;

    private LocalDateTime requestedAt;

    public InterviewRequest() {
    }

    @PrePersist
    public void setDefaultValues() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }

        if (interviewStatus == null || interviewStatus.isBlank()) {
            interviewStatus = "PENDING";
        }
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public Long getTestId() {
        return testId;
    }

    public Double getAptitudePercentage() {
        return aptitudePercentage;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public String getInterviewTime() {
        return interviewTime;
    }

    public String getInterviewMode() {
        return interviewMode;
    }

    public String getInterviewLink() {
        return interviewLink;
    }

    public String getAdminRemark() {
        return adminRemark;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public void setAptitudePercentage(Double aptitudePercentage) {
        this.aptitudePercentage = aptitudePercentage;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public void setInterviewTime(String interviewTime) {
        this.interviewTime = interviewTime;
    }

    public void setInterviewMode(String interviewMode) {
        this.interviewMode = interviewMode;
    }

    public void setInterviewLink(String interviewLink) {
        this.interviewLink = interviewLink;
    }

    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }
}