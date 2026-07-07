package com.recruitease.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.TestQuestion;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByTestId(Long testId);

    boolean existsByTestIdAndQuestionIgnoreCase(Long testId, String question);
}