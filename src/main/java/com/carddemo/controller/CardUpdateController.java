package com.carddemo.controller;

import com.carddemo.model.request.CardUpdateRequest;
import com.carddemo.model.response.CardUpdateResponse;
import com.carddemo.service.CardUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Credit Card Update operations
 *
 * Migrated from COBOL COCRDUPC.cbl CICS BMS Screen interactions
 *
 * COBOL CICS Command → REST API mapping:
 * ──────────────────────────────────────────────────────────────
 * EXEC CICS RECEIVE MAP    → GET  /api/v1/cards/{cardNum}
 * EXEC CICS SEND MAP       → Response body
 * EXEC CICS RETURN         → HTTP Response codes
 * PROCESS-ENTER-KEY        → PUT  /api/v1/cards/{cardNum}
 * STARTBR-CARD-FILE        → GET  /api/v1/cards/customer/{custId}
 */
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Credit Card Update",
        description = "APIs migrated from COBOL COCRDUPC.cbl - Credit Card Update Program")
public class CardUpdateController {

    private final CardUpdateService cardUpdateService;

    /**
     * Migrated from COBOL: READ-PROCESSING paragraph
     * GET card data for update screen population
     *
     * COBOL: EXEC CICS READ FILE('CARDDAT') INTO(WS-CARD-RID)
     */
    @GetMapping("/{cardNum}")
    @Operation(summary = "Get card for update",
            description = "Retrieve credit card details for update. " +
                    "Migrated from COBOL READ-PROCESSING paragraph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "400", description = "Invalid card number format")
    })
    public ResponseEntity<CardUpdateResponse> getCardForUpdate(
            @Parameter(description = "16-digit card number", required = true)
            @PathVariable String cardNum) {

        log.info("GET card request received");
        CardUpdateResponse response = cardUpdateService.getCardForUpdate(cardNum);
        return ResponseEntity.ok(response);
    }

    /**
     * Migrated from COBOL: PROCESS-ENTER-KEY paragraph
     * Update credit card record
     *
     * COBOL: EXEC CICS REWRITE FILE('CARDDAT') FROM(WS-CARD-FORUPD-REC)
     */
    @PutMapping("/{cardNum}")
    @Operation(summary = "Update credit card",
            description = "Update credit card details. " +
                    "Migrated from COBOL PROCESS-ENTER-KEY paragraph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Conflict - no changes or mismatch")
    })
    public ResponseEntity<CardUpdateResponse> updateCard(
            @Parameter(description = "16-digit card number", required = true)
            @PathVariable String cardNum,
            @Valid @RequestBody CardUpdateRequest request) {

        log.info("PUT card update request received");
        CardUpdateResponse response = cardUpdateService.updateCard(cardNum, request);

        // Map COBOL WS-RETURN-CODE to HTTP status
        if ("0008".equals(response.getReturnCode())) {
            // No changes - COBOL equivalent: no REWRITE performed
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Migrated from COBOL: STARTBR-CARD-FILE + browse logic
     * List all cards for a customer
     */
    @GetMapping("/customer/{custId}")
    @Operation(summary = "Get cards by customer",
            description = "List all cards for a customer. " +
                    "Migrated from COBOL STARTBR-CARD-FILE paragraph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cards retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Page<CardUpdateResponse>> getCardsByCustomer(
            @Parameter(description = "Customer ID (9 digits)", required = true)
            @PathVariable Long custId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "cardNum") String sortBy) {

        Page<CardUpdateResponse> response = cardUpdateService.getCardsByCustomer(
                custId, PageRequest.of(page, size, Sort.by(sortBy)));
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a card
     * Migrated from COBOL: card status = '0' update logic
     */
    @PatchMapping("/{cardNum}/deactivate")
    @Operation(summary = "Deactivate credit card",
            description = "Set card status to Inactive (0). " +
                    "Migrated from COBOL deactivation logic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardUpdateResponse> deactivateCard(
            @PathVariable String cardNum,
            @RequestParam(required = false, defaultValue = "User requested") String reason) {

        CardUpdateResponse response = cardUpdateService.deactivateCard(cardNum, reason);
        return ResponseEntity.ok(response);
    }
}