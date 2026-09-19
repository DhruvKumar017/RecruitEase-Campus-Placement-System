package com.recruitease.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "student_notification_reads",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "studentId",
                                "notificationKey"
                        }
                )
        }
)
public class StudentNotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private String notificationKey;

    private boolean readStatus;

    private LocalDateTime readAt;

    public StudentNotificationRead() {
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}