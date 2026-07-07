package com.recruitease.dto;

public class ApplicationAdminDTO {

    private Long id;
    private Long studentId;
    private Long companyId;
    private Long driveId;

    private String studentName;
    private String companyName;
    private String role;
    private String status;

    private Long testId;
    private String testDate;
    private String testStartTime;
    private String testEndTime;
    private String testPermission;

    private Integer obtainedMarks;
    private Integer totalMarks;
    private Double percentage;
    private Double companyCutoff;
    private String testResultStatus;

    private boolean interviewRequested;
    private String interviewStatus;

    public ApplicationAdminDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDriveId() {
        return driveId;
    }

    public void setDriveId(Long driveId) {
        this.driveId = driveId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getTestStartTime() {
        return testStartTime;
    }

    public void setTestStartTime(String testStartTime) {
        this.testStartTime = testStartTime;
    }

    public String getTestEndTime() {
        return testEndTime;
    }

    public void setTestEndTime(String testEndTime) {
        this.testEndTime = testEndTime;
    }

    public String getTestPermission() {
        return testPermission;
    }

    public void setTestPermission(String testPermission) {
        this.testPermission = testPermission;
    }

    public Integer getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(Integer obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Double getCompanyCutoff() {
        return companyCutoff;
    }

    public void setCompanyCutoff(Double companyCutoff) {
        this.companyCutoff = companyCutoff;
    }

    public String getTestResultStatus() {
        return testResultStatus;
    }

    public void setTestResultStatus(String testResultStatus) {
        this.testResultStatus = testResultStatus;
    }

    public boolean isInterviewRequested() {
        return interviewRequested;
    }

    public void setInterviewRequested(boolean interviewRequested) {
        this.interviewRequested = interviewRequested;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }
}