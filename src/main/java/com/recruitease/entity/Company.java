package com.recruitease.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private Double minCgpa;
    private String requiredSkills;
    private Double aptitudeCutoff;
    public Long getId() {
    return id;
}

public String getCompanyName() {
    return companyName;
}

public void setCompanyName(String companyName) {
    this.companyName = companyName;
}

public Double getMinCgpa() {
    return minCgpa;
}

public void setMinCgpa(Double minCgpa) {
    this.minCgpa = minCgpa;
}

public String getRequiredSkills() {
    return requiredSkills;
}

public void setRequiredSkills(String requiredSkills) {
    this.requiredSkills = requiredSkills;
}
public Double getAptitudeCutoff() {
    return aptitudeCutoff;
}

public void setAptitudeCutoff(Double aptitudeCutoff) {
    this.aptitudeCutoff = aptitudeCutoff;
}
}