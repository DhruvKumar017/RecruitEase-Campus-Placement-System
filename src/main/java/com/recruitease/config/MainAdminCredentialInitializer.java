package com.recruitease.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.recruitease.entity.MainAdminCredential;
import com.recruitease.repository.MainAdminCredentialRepository;

@Configuration
public class MainAdminCredentialInitializer {

    @Value("${app.main-admin.username}")
    private String mainAdminUsername;

    @Value("${app.main-admin.password}")
    private String mainAdminPassword;

    @Value("${app.main-admin.email}")
    private String mainAdminEmail;

    @Bean
    CommandLineRunner createMainAdminCredential(
            MainAdminCredentialRepository mainAdminCredentialRepository) {

        return args -> {

            boolean alreadyExists = mainAdminCredentialRepository
                    .findTopByUsernameIgnoreCase(mainAdminUsername)
                    .isPresent();

            if (alreadyExists) {
                return;
            }

            MainAdminCredential mainAdmin = new MainAdminCredential();

            mainAdmin.setUsername(mainAdminUsername.trim());
            mainAdmin.setPassword(mainAdminPassword.trim());
            mainAdmin.setEmail(mainAdminEmail.trim());

            mainAdminCredentialRepository.save(mainAdmin);

            System.out.println("Main Admin credential created successfully.");
        };
    }
}