package com.recruitease.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.StudentIssue;

public interface StudentIssueRepository
        extends JpaRepository<StudentIssue, Long> {

    List<StudentIssue> findByStatusIgnoreCase(String status);
}