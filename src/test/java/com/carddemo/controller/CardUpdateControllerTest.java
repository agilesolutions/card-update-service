package com.carddemo.controller;

import com.carddemo.model.request.CardUpdateRequest;
import com.carddemo.model.response.CardUpdateResponse;
import com.carddemo.service.CardUpdateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CardUpdateController
 * Tests migrated business scenarios from COBOL COCRDUPC.cbl
 */
@WebMvcTest(CardUpdateController.class)
@DisplayName("CardUpdateController Tests - Migrated from COBOL COCRDUPC")
class CardUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardUpdateService cardUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    private CardUpdateRequest validRequest;
    private CardUpdateResponse successResponse;
    private static final String VALID_CARD_NUM = "4111111111111111";

    @BeforeEach
    void setUp() {
        validRequest = CardUpdateRequest.builder()
                .cardNum(VALID_CARD_NUM)
                .custId(100000001L)
                .cardStatus("1")
                .cardEmbossedName("JOHN SMITH")
                .cardExpirationDate(LocalDate.of(2027, 12, 31))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .groupId("GRP001")
                .cardActiveStatus("Y")
                .build();

        successResponse = CardUpdateResponse.builder()
                .status("SUCCESS")
                .message("Card updated successfully")
                .returnCode("0000")
                .cardDetails(CardUpdateResponse.CardDetails.builder()
                        .cardNum("************1111")
                        .custId(100000001L)
                        .cardStatus("1")
                        .build())
                .build();
    }

    /**
     * Tests COBOL: READ-PROCESSING paragraph
     * EXEC CICS READ FILE('CARDDAT') - successful read
     */
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET card - should return card details (COBOL: READ-PROCESSING)")
    void getCardForUpdate_Success() throws Exception {
        when(cardUpdateService.getCardForUpdate(VALID_CARD_NUM))
                .thenReturn(successResponse);

        mockMvc.perform(get("/api/v1/cards/{cardNum}", VALID_CARD_NUM)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.returnCode").value("0000"))
                .andExpect(jsonPath("$.cardDetails.custId").value(100000001));
    }

    /**
     * Tests COBOL: PROCESS-ENTER-KEY paragraph
     * EXEC CICS REWRITE FILE('CARDDAT') - successful update
     */
    @Test
    @WithMockUser(roles = "CARD