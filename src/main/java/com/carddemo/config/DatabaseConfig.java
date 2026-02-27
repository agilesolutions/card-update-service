package com.carddemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database Configuration
 * Replaces COBOL EXEC CICS/DB2 connection management
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.carddemo.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Connection pool and transaction management handled by Spring Boot auto-configuration
    // Flyway migration auto-configured via spring.flyway properties in application.yml
}