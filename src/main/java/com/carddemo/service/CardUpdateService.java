package com.carddemo.service;

import com.carddemo.model.request.CardUpdateRequest;
import com.carddemo.model.response.CardUpdateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Credit Card Update operations
 * Migrated from COBOL COCRDUPC.cbl main processing logic
 */
public interface CardUpdateService {

    /**
     * Migrated from COBOL: MAIN-PARA / PROCESS-ENTER-KEY
     * Full update of credit card record
     */
    CardUpdateResponse updateCard(String cardNum, CardUpdateRequest request);

    /**
     * Migrated from COBOL: READ-PROCESSING / STARTBR-CARD-FILE
     * Retrieve card for display/update form population
     */
    CardUpdateResponse getCardForUpdate(String cardNum);

    /**
     * Migrated from COBOL: STARTBR-CARD-FILE with customer browse
     * List all cards for a customer
     */
    Page<CardUpdateResponse> getCardsByCustomer(Long custId, Pageable pageable);

    /**
     * Migrated from COBOL: DELETE processing (if applicable)
     * Soft delete / deactivate card
     */
    CardUpdateResponse deactivateCard(String cardNum, String reason);
}