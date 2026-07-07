package com.recruitease.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "portal_settings")
public class PortalSettings {

    @Id
    private Long id;

    private String instituteName;
    private String instituteCode;

    @Lob
    private String instituteAddress;

    private String contactEmail;
    private String contactPhone;
    private String website;

    /*
     * Sender details only.
     * SMTP/Gmail password database me save nahi hoga.
     */
    private String senderName;
    private String senderEmail;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpEncryption;
    private String replyToEmail;

    private boolean notifyStudentRegister = true;
    private boolean notifyCompanyAdd = true;
    private boolean notifyApplication = true;
    private boolean notifyTest = true;
    private boolean notifyPlacement = true;

    private String placementSession;
    private String defaultApplicationStatus;
    private Integer maxResumeSize;
    private String studentRegistrationMode;

    @Lob
    private String systemAnnouncement;

    private LocalDateTime updatedAt;

    public PortalSettings() {
    }

    @PrePersist
    public void beforeSave() {
        applyDefaultValues();

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private void applyDefaultValues() {
        if (instituteName == null || instituteName.isBlank()) {
            instituteName = "RecruitEase Institute of Technology";
        }

        if (instituteCode == null || instituteCode.isBlank()) {
            instituteCode = "RECRUIT2026";
        }

        if (contactEmail == null || contactEmail.isBlank()) {
            contactEmail = "admin@recruitease.com";
        }

        if (placementSession == null || placementSession.isBlank()) {
            placementSession = "2026 - 2027";
        }

        if (defaultApplicationStatus == null ||
                defaultApplicationStatus.isBlank()) {
            defaultApplicationStatus = "Applied";
        }

        if (maxResumeSize == null || maxResumeSize <= 0) {
            maxResumeSize = 5;
        }

        if (studentRegistrationMode == null ||
                studentRegistrationMode.isBlank()) {
            studentRegistrationMode = "Open";
        }

        if (smtpPort == null || smtpPort <= 0) {
            smtpPort = 587;
        }

        if (smtpEncryption == null || smtpEncryption.isBlank()) {
            smtpEncryption = "TLS";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getInstituteCode() {
        return instituteCode;
    }

    public void setInstituteCode(String instituteCode) {
        this.instituteCode = instituteCode;
    }

    public String getInstituteAddress() {
        return instituteAddress;
    }

    public void setInstituteAddress(String instituteAddress) {
        this.instituteAddress = instituteAddress;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpEncryption() {
        return smtpEncryption;
    }

    public void setSmtpEncryption(String smtpEncryption) {
        this.smtpEncryption = smtpEncryption;
    }

    public String getReplyToEmail() {
        return replyToEmail;
    }

    public void setReplyToEmail(String replyToEmail) {
        this.replyToEmail = replyToEmail;
    }

    public boolean isNotifyStudentRegister() {
        return notifyStudentRegister;
    }

    public void setNotifyStudentRegister(boolean notifyStudentRegister) {
        this.notifyStudentRegister = notifyStudentRegister;
    }

    public boolean isNotifyCompanyAdd() {
        return notifyCompanyAdd;
    }

    public void setNotifyCompanyAdd(boolean notifyCompanyAdd) {
        this.notifyCompanyAdd = notifyCompanyAdd;
    }

    public boolean isNotifyApplication() {
        return notifyApplication;
    }

    public void setNotifyApplication(boolean notifyApplication) {
        this.notifyApplication = notifyApplication;
    }

    public boolean isNotifyTest() {
        return notifyTest;
    }

    public void setNotifyTest(boolean notifyTest) {
        this.notifyTest = notifyTest;
    }

    public boolean isNotifyPlacement() {
        return notifyPlacement;
    }

    public void setNotifyPlacement(boolean notifyPlacement) {
        this.notifyPlacement = notifyPlacement;
    }

    public String getPlacementSession() {
        return placementSession;
    }

    public void setPlacementSession(String placementSession) {
        this.placementSession = placementSession;
    }

    public String getDefaultApplicationStatus() {
        return defaultApplicationStatus;
    }

    public void setDefaultApplicationStatus(String defaultApplicationStatus) {
        this.defaultApplicationStatus = defaultApplicationStatus;
    }

    public Integer getMaxResumeSize() {
        return maxResumeSize;
    }

    public void setMaxResumeSize(Integer maxResumeSize) {
        this.maxResumeSize = maxResumeSize;
    }

    public String getStudentRegistrationMode() {
        return studentRegistrationMode;
    }

    public void setStudentRegistrationMode(String studentRegistrationMode) {
        this.studentRegistrationMode = studentRegistrationMode;
    }

    public String getSystemAnnouncement() {
        return systemAnnouncement;
    }

    public void setSystemAnnouncement(String systemAnnouncement) {
        this.systemAnnouncement = systemAnnouncement;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}