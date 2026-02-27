package com.carddemo.repository;

import com.carddemo.model.entity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CustomerData entity
 * Replaces COBOL VSAM file CUSTDAT operations
 */
@Repository
public interface CustomerDataRepository extends JpaRepository<CustomerData, Long> {

    /**
     * Replaces: READ CUSTDAT INTO WS-CUST-DATA
     */
    Optional<CustomerData> findByCustomerId(Long customerId);

    /**
     * Check customer existence - mirrors COBOL NOTFND check
     */
    boolean existsByCustomerId(Long customerId);
}