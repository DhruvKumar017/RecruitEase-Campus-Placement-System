package com.recruitease.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aptitude_tests")
public class AptitudeTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String testName;
    private String testType;
    private Integer duration;
    private String status;
    private Integer score;

    // 👇 YAHAN ADD KARNA HAI
    private String scheduledDay;

    public AptitudeTest() {}

    // Getters
    public Long getId() { return id; }
    public String getTestName() { return testName; }
    public String getTestType() { return testType; }
    public Integer getDuration() { return duration; }
    public String getStatus() { return status; }
    public Integer getScore() { return score; }

    // 👇 GETTER YAHAN ADD KARO (score ke niche)
    public String getScheduledDay() {
        return scheduledDay;
    }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTestName(String testName) { this.testName = testName; }
    public void setTestType(String testType) { this.testType = testType; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public void setStatus(String status) { this.status = status; }
    public void setScore(Integer score) { this.score = score; }

    // 👇 SETTER SABSE LAST ME ADD KARO
    public void setScheduledDay(String scheduledDay) {
        this.scheduledDay = scheduledDay;
    }
}