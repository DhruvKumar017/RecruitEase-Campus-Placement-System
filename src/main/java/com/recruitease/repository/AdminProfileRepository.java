package com.recruitease.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.AdminProfile;

public interface AdminProfileRepository
        extends JpaRepository<AdminProfile, Long> {

    Optional<AdminProfile> findByUsernameIgnoreCase(String username);
}