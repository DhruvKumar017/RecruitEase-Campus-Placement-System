package com.recruitease.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "admin_notification_reads",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_admin_notification_read",
                        columnNames = {
                                "admin_username",
                                "notification_key"
                        }
                )
        }
)
public class AdminNotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_username", nullable = false)
    private String adminUsername;

    @Column(name = "notification_key", nullable = false)
    private String notificationKey;

    @Column(nullable = false)
    private LocalDateTime readAt;

    public AdminNotificationRead() {
    }

    @PrePersist
    public void beforeSave() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}