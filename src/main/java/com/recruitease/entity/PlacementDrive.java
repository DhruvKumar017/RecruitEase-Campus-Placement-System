package com.recruitease.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "placement_drives")
public class PlacementDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String jobRole;
    private Double packageLpa;
    private String driveDate;
    private String lastDate;
    private String mode;
    private String location;
    private String branch;
    private Double minCgpa;
    private String skills;
    private Integer vacancy;
    private String status;

    public PlacementDrive() {}

    public Long getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public Double getPackageLpa() {
        return packageLpa;
    }

    public String getDriveDate() {
        return driveDate;
    }

    public String getLastDate() {
        return lastDate;
    }

    public String getMode() {
        return mode;
    }

    public String getLocation() {
        return location;
    }

    public String getBranch() {
        return branch;
    }

    public Double getMinCgpa() {
        return minCgpa;
    }

    public String getSkills() {
        return skills;
    }

    public Integer getVacancy() {
        return vacancy;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public void setPackageLpa(Double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public void setDriveDate(String driveDate) {
        this.driveDate = driveDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setMinCgpa(Double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void setVacancy(Integer vacancy) {
        this.vacancy = vacancy;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}