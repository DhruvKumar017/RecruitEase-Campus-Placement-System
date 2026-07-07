package com.recruitease.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_test_results")
public class StudentTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long applicationId;
    private Long testId;

    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer obtainedMarks;
    private Integer totalMarks;

    private Double percentage;
    private String status;

    private LocalDateTime submittedAt;

    public StudentTestResult() {}

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getApplicationId() { return applicationId; }
    public Long getTestId() { return testId; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public Integer getObtainedMarks() { return obtainedMarks; }
    public Integer getTotalMarks() { return totalMarks; }
    public Double getPercentage() { return percentage; }
    public String getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public void setId(Long id) { this.id = id; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public void setTestId(Long testId) { this.testId = testId; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
    public void setObtainedMarks(Integer obtainedMarks) { this.obtainedMarks = obtainedMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    public void setStatus(String status) { this.status = status; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}