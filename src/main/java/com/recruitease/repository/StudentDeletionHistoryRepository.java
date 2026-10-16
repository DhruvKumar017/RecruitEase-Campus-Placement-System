package com.recruitease.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recruitease.entity.StudentDeletionHistory;

@Repository
public interface StudentDeletionHistoryRepository
        extends JpaRepository<StudentDeletionHistory, Long> {

    List<StudentDeletionHistory> findByActionOrderByActionAtDesc(String action);
}