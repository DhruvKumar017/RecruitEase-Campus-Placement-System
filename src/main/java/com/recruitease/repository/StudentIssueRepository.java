package com.recruitease.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.StudentIssue;

public interface StudentIssueRepository
        extends JpaRepository<StudentIssue, Long> {

    List<StudentIssue> findByStatusIgnoreCase(String status);

// Retrieve complaints linked to a student account
List<StudentIssue> findByStudentIdOrderBySubmittedAtDesc(
        Long studentId
);

// Retrieve public Help Center complaints by registered email
List<StudentIssue> findByStudentEmailIgnoreCaseOrderBySubmittedAtDesc(
        String studentEmail
);
}