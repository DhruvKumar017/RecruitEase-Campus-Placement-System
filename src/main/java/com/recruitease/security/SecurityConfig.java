package com.recruitease.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.security.enforce-jwt:false}")
    private boolean enforceJwt;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .formLogin(AbstractHttpConfigurer::disable)

            .httpBasic(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(authorize -> {

                /*
                 * Abhi compatibility mode hai.
                 * Old pages bina token ke bhi chalenge.
                 */
                if (!enforceJwt) {
                    authorize.anyRequest().permitAll();
                    return;
                }

                /*
                 * Later strict mode me ye rules active honge.
                 */
                authorize
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/*.html",
                        "/*.css",
                        "/*.js",
                        "/uploads/**",
                        "/api/admin-login/**",
                        "/api/student-login/**",
                        "/api/students/register",
                        "/api/student-issues"
                    )
                    .permitAll()

                    .requestMatchers(
                        "/api/admin-settings/**",
                        "/api/admin-notifications/**"
                    )
                    .hasAnyRole("MAIN_ADMIN", "ADMIN")

                    .requestMatchers("/api/**")
                    .authenticated()

                    .anyRequest()
                    .permitAll();
            })

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}