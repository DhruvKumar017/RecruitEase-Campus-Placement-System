package com.recruitease.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.AptitudeTest;

public interface AptitudeTestRepository
        extends JpaRepository<AptitudeTest, Long> {

}