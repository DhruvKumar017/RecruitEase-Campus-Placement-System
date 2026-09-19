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
@Table(name="student_notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private String title;

    @Column(length=1000)
    private String message;

    private String type;

    private boolean readStatus=false;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        createdAt=LocalDateTime.now();
    }

    public Notification(){}

    public Long getId(){ return id; }

    public Long getStudentId(){ return studentId; }

    public String getTitle(){ return title; }

    public String getMessage(){ return message; }

    public String getType(){ return type; }

    public boolean isReadStatus(){ return readStatus; }

    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setId(Long id){ this.id=id; }

    public void setStudentId(Long studentId){ this.studentId=studentId; }

    public void setTitle(String title){ this.title=title; }

    public void setMessage(String message){ this.message=message; }

    public void setType(String type){ this.type=type; }

    public void setReadStatus(boolean readStatus){
        this.readStatus=readStatus;
    }

    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt=createdAt;
    }

}