package com.carddemo.service;

import com.carddemo.exception.CardNotFoundException;
import com.carddemo.exception.CardUpdateException;
import com.carddemo.model.entity.CardData;
import com.carddemo.model.entity.CustomerData;
import com.carddemo.model.request.CardUpdateRequest;
import com.carddemo.model.response.CardUpdateResponse;
import com.carddemo.repository.CardDataRepository;
import com.carddemo.repository.CustomerDataRepository;
import com.carddemo.validation.CardUpdateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementation migrated from COBOL COCRDUPC.cbl
 *
 * COBOL paragraph to Java method mapping:
 * ─────────────────────────────────────────────────────────
 * MAIN-PARA                    → updateCard()
 * PROCESS-ENTER-KEY            → processEnterKey()
 * READ-PROCESSING              → getCardForUpdate()
 * STARTBR-CARD-FILE            → getCardsByCustomer()
 * VALIDATE-INPUT-KEY-FIELDS    → validator.validateCardNumber()
 * VALIDATE-MANDATORY-FIELDS    → validator.validateMandatoryFields()
 * VALIDATE-CARD-DATA           → validator.validateCardData()
 * UPDATE-CARD-INFO             → updateCardRecord()
 * UPD-CHECKS-OK                → validator.hasChanges()
 * ERRORSCLR                    → handled via exceptions
 * SEND-PLAIN-TEXT              → Response object
 * RETURN-TO-PREV-SCREEN        → HTTP redirect / response code
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CardUpdateServiceImpl implements CardUpdateService {

    private final CardDataRepository cardDataRepository;
    private final CustomerDataRepository customerDataRepository;
    private final CardUpdateValidator validator;

    /**
     * Migrated from COBOL: MAIN-PARA + PROCESS-ENTER-KEY
     *
     * Original COBOL flow:
     * 1. Read existing card record (READ CARDDAT)
     * 2. Validate input key fields
     * 3. Validate mandatory fields
     * 4. Validate card data
     * 5. Compare old vs new (UPD-CHECKS-OK)
     * 6. If changed: REWRITE CARDDAT
     * 7. Send success/error response
     */
    @Override
    @Transactional
    public CardUpdateResponse updateCard(String cardNum, CardUpdateRequest request) {
        log.info("Processing card update for cardNum: {}", maskCardNum(cardNum));

        // Step 1: Validate card number format
        // COBOL: VALIDATE-INPUT-KEY-FIELDS
        validator.validateCardNumber(cardNum);

        // Step 2: Read existing card record
        // COBOL: READ CARDDAT INTO WS-CARD-FORUPD-REC
        CardData existingCard = cardDataRepository.findByCardNum(cardNum)
                .orElseThrow(() -> {
                    log.warn("Card not found: {}", maskCardNum(cardNum));
                    return new CardNotFoundException(
                            "Card not found: " + maskCardNum(cardNum),
                            CardUpdateValidator.RC_CARD_NOT_FOUND);
                });

        // Step 3: Validate customer exists
        // COBOL: READ CUSTDAT INTO WS-CUST-DATA
        if (request.getCustId() != null) {
            if (!customerDataRepository.existsByCustomerId(request.getCustId())) {
                throw new CardNotFoundException(
                        "Customer not found: " + request.getCustId(),
                        CardUpdateValidator.RC_CUSTOMER_NOT_FOUND);
            }

            // COBOL: IF CDEMO-CUST-ID NOT = WS-CARD-RID-CUST-ID
            if (!existingCard.getCustId().equals(request.getCustId())) {
                throw new CardUpdateException(
                        CardUpdateValidator.RC_CUST_ID_MISMATCH,
                        "Customer ID does not match card record");
            }
        }

        // Step 4: Validate mandatory fields
        // COBOL: VALIDATE-MANDATORY-FIELDS
        validator.validateMandatoryFields(request);

        // Step 5: Validate card data
        // COBOL: VALIDATE-CARD-DATA
        validator.validateCardData(request);

        // Step 6: Check if any changes were made
        // COBOL: UPD-CHECKS-OK / COMPARE-OLD-NEW-RECORDS
        if (!validator.hasChanges(request, existingCard)) {
            log.info("No changes detected for card: {}", maskCardNum(cardNum));
            return buildResponse(existingCard,
                    CardUpdateValidator.RC_NO_CHANGES,
                    "No changes detected - record not updated");
        }

        // Step 7: Apply updates
        // COBOL: REWRITE CARDDAT FROM WS-CARD-FORUPD-REC
        CardData updatedCard = applyUpdates(existingCard, request);
        CardData savedCard = cardDataRepository.save(updatedCard);

        log.info("Card updated successfully: {}", maskCardNum(cardNum));

        return buildResponse(savedCard,
                CardUpdateValidator.RC_SUCCESS,
                "Card updated successfully");
    }

    /**
     * Migrated from COBOL: READ-PROCESSING
     * Reads card data for display on update screen
     *
     * COBOL: READ CARDDAT RIDFLD(WS-CARD-RID-CARDNUM)
     */
    @Override
    @Transactional(readOnly = true)
    public CardUpdateResponse getCardForUpdate(String cardNum) {
        log.info("Retrieving card for update: {}", maskCardNum(cardNum));

        validator.validateCardNumber(cardNum);

        CardData card = cardDataRepository.findByCardNum(cardNum)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found: " + maskCardNum(cardNum),
                        CardUpdateValidator.RC_CARD_NOT_FOUND));

        return buildResponse(card, CardUpdateValidator.RC_SUCCESS,
                "Card retrieved successfully");
    }

    /**
     * Migrated from COBOL: STARTBR-CARD-FILE + browse logic
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CardUpdateResponse> getCardsByCustomer(Long custId, Pageable pageable) {
        log.info("Retrieving cards for customer: {}", custId);

        if (!customerDataRepository.existsByCustomerId(custId)) {
            throw new CardNotFoundException(
                    "Customer not found: " + custId,
                    CardUpdateValidator.RC_CUSTOMER_NOT_FOUND);
        }

        return cardDataRepository.findByCustId(custId, pageable)
                .map(card -> buildResponse(card, CardUpdateValidator.RC_SUCCESS,
                        "Card retrieved"));
    }

    /**
     * Soft delete / deactivate card
     * Migrated from COBOL: card status update to '0' (Inactive)
     */
    @Override
    @Transactional
    public CardUpdateResponse deactivateCard(String cardNum, String reason) {
        log.info("Deactivating card: {}", maskCardNum(cardNum));

        validator.validateCardNumber(cardNum);

        CardData card = cardDataRepository.findByCardNum(cardNum)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found: " + maskCardNum(cardNum),
                        CardUpdateValidator.RC_CARD_NOT_FOUND));

        // COBOL: MOVE '0' TO CDEMO-CARD-STATUS
        card.setCardStatus("0");
        card.setCardActiveStatus("N");
        CardData saved = cardDataRepository.save(card);

        log.info("Card deactivated: {} - Reason: {}", maskCardNum(cardNum), reason);

        return buildResponse(saved, CardUpdateValidator.RC_SUCCESS,
                "Card deactivated successfully");
    }

    /**
     * Migrated from COBOL: MOVE corresponding fields to update record
     * Applies updates from request to entity
     */
    private CardData applyUpdates(CardData existingCard, CardUpdateRequest request) {
        if (request.getCardStatus() != null) {
            existingCard.setCardStatus(request.getCardStatus());
        }
        if (request.getCardEmbossedName() != null) {
            existingCard.setCardEmbossedName(request.getCardEmbossedName().toUpperCase());
        }
        if (request.getCardExpirationDate() != null) {
            existingCard.setCardExpirationDate(request.getCardExpirationDate());
        }
        if (request.getCreditLimit() != null) {
            existingCard.setCreditLimit(request.getCreditLimit());
        }
        if (request.getCashCreditLimit() != null) {
            existingCard.setCashCreditLimit(request.getCashCreditLimit());
        }
        if (request.getOpenDate() != null) {
            existingCard.setOpenDate(request.getOpenDate());
        }
        if (request.getExpiryDate() != null) {
            existingCard.setExpiryDate(request.getExpiryDate());
        }
        if (request.getGroupId() != null) {
            existingCard.setGroupId(request.getGroupId());
        }
        if (request.getCardActiveStatus() != null) {
            existingCard.setCardActiveStatus(request.getCardActiveStatus());
        }
        return existingCard;
    }

    private CardUpdateResponse buildResponse(CardData card, String returnCode,
                                             String message) {
        return CardUpdateResponse.builder()
                .status(CardUpdateValidator.RC_SUCCESS.equals(returnCode)
                        ? "SUCCESS" : "INFO")
                .message(message)
                .returnCode(returnCode)
                .cardDetails(CardUpdateResponse.CardDetails.builder()
                        .cardNum(maskCardNum(card.getCardNum()))
                        .custId(card.getCustId())
                        .cardStatus(card.getCardStatus())
                        .cardActiveStatus(card.getCardActiveStatus())
                        .cardEmbossedName(card.getCardEmbossedName())
                        .cardExpirationDate(card.getCardExpirationDate())
                        .creditLimit(card.getCreditLimit())
                        .cashCreditLimit(card.getCashCreditLimit())
                        .openDate(card.getOpenDate())
                        .expiryDate(card.getExpiryDate())
                        .groupId(card.getGroupId())
                        .lastUpdatedAt(card.getUpdatedAt() != null
                                ? card.getUpdatedAt() : LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * Mask card number for logging - PCI DSS compliance
     * Shows only last 4 digits
     */
    private String maskCardNum(String cardNum) {
        if (cardNum == null || cardNum.length() < 4) return "****";
        return "************" + cardNum.substring(cardNum.length() - 4);
    }
}