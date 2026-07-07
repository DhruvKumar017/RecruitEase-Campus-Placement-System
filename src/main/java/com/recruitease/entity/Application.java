package com.recruitease.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long companyId;
    private Long driveId;

    private String companyName;
    private String role;
    private String status;

    private Long testId;
    private String testDate;
    private String testStartTime;
    private String testEndTime;
    private String testPermission;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Application() {
    }

    @PrePersist
    public void beforeSave() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.trim().isEmpty()) {
            status = "Applied";
        }
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getDriveId() {
        return driveId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public Long getTestId() {
        return testId;
    }

    public String getTestDate() {
        return testDate;
    }

    public String getTestStartTime() {
        return testStartTime;
    }

    public String getTestEndTime() {
        return testEndTime;
    }

    public String getTestPermission() {
        return testPermission;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setDriveId(Long driveId) {
        this.driveId = driveId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public void setTestStartTime(String testStartTime) {
        this.testStartTime = testStartTime;
    }

    public void setTestEndTime(String testEndTime) {
        this.testEndTime = testEndTime;
    }

    public void setTestPermission(String testPermission) {
        this.testPermission = testPermission;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}