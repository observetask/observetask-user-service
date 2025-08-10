package com.observetask.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoding configuration for ObserveTask User Service
 * Configures BCrypt with 12 salt rounds for secure password hashing
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt password encoder with 12 salt rounds
     * 12 rounds provides strong security while maintaining reasonable performance
     * 
     * Performance reference:
     * - 10 rounds: ~100ms per hash
     * - 12 rounds: ~400ms per hash (recommended for 2024)
     * - 14 rounds: ~1.6s per hash
     * 
     * @return BCryptPasswordEncoder configured with 12 salt rounds
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}