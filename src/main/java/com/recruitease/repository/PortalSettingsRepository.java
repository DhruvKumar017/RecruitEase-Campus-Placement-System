package com.recruitease.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitease.entity.PortalSettings;

public interface PortalSettingsRepository
        extends JpaRepository<PortalSettings, Long> {
}