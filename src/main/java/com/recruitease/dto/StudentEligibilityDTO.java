package com.recruitease.dto;

import java.util.List;

public class StudentEligibilityDTO {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String branch;
    private Double cgpa;
    private String skills;
    private int eligibleCompanyCount;
    private List<String> eligibleCompanies;

    public StudentEligibilityDTO() {
    }

    public StudentEligibilityDTO(
            Long studentId,
            String studentName,
            String rollNumber,
            String branch,
            Double cgpa,
            String skills,
            int eligibleCompanyCount,
            List<String> eligibleCompanies
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.branch = branch;
        this.cgpa = cgpa;
        this.skills = skills;
        this.eligibleCompanyCount = eligibleCompanyCount;
        this.eligibleCompanies = eligibleCompanies;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getEligibleCompanyCount() {
        return eligibleCompanyCount;
    }

    public void setEligibleCompanyCount(int eligibleCompanyCount) {
        this.eligibleCompanyCount = eligibleCompanyCount;
    }

    public List<String> getEligibleCompanies() {
        return eligibleCompanies;
    }

    public void setEligibleCompanies(List<String> eligibleCompanies) {
        this.eligibleCompanies = eligibleCompanies;
    }
}