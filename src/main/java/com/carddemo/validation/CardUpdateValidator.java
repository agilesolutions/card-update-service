package com.carddemo.validation;

import com.carddemo.exception.CardUpdateException;
import com.carddemo.model.request.CardUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business validation logic migrated from COBOL COCRDUPC.cbl
 *
 * Original COBOL paragraphs migrated:
 *  - VALIDATE-INPUT-KEY-FIELDS
 *  - VALIDATE-MANDATORY-FIELDS
 *  - VALIDATE-CARD-DATA
 *  - UPD-CHECKS-OK
 *  - ERRORSCLR
 */
@Component
@Slf4j
public class CardUpdateValidator {

    // Mapped from COBOL WS-RETURN-CODE values
    public static final String RC_SUCCESS = "0000";
    public static final String RC_CARD_NOT_FOUND = "0001";
    public static final String RC_CUSTOMER_NOT_FOUND = "0002";
    public static final String RC_INVALID_CARD_STATUS = "0003";
    public static final String RC_INVALID_EXPIRY_DATE = "0004";
    public static final String RC_INVALID_CREDIT_LIMIT = "0005";
    public static final String RC_MANDATORY_FIELD_MISSING = "0006";
    public static final String RC_CARD_NUM_INVALID = "0007";
    public static final String RC_NO_CHANGES = "0008";
    public static final String RC_CUST_ID_MISMATCH = "0009";
    public static final String RC_CASH_LIMIT_EXCEEDS_CREDIT = "0010";

    /**
     * Migrated from COBOL: VALIDATE-INPUT-KEY-FIELDS
     * Validates card number format
     */
    public void validateCardNumber(String cardNum) {
        List<String> errors = new ArrayList<>();

        if (cardNum == null || cardNum.isBlank()) {
            errors.add("Card number is required");
        } else if (!cardNum.matches("^[0-9]{16}$")) {
            errors.add("Card number must be 16 numeric digits");
        } else if (!isValidLuhn(cardNum)) {
            errors.add("Card number fails Luhn check");
        }

        if (!errors.isEmpty()) {
            throw new CardUpdateException(RC_CARD_NUM_INVALID,
                    String.join("; ", errors));
        }
    }

    /**
     * Migrated from COBOL: VALIDATE-MANDATORY-FIELDS
     * Checks all required fields are present
     */
    public void validateMandatoryFields(CardUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getCustId() == null) {
            errors.add("Customer ID is mandatory");
        }
        if (request.getCardStatus() == null || request.getCardStatus().isBlank()) {
            errors.add("Card status is mandatory");
        }

        if (!errors.isEmpty()) {
            throw new CardUpdateException(RC_MANDATORY_FIELD_MISSING,
                    String.join("; ", errors));
        }
    }

    /**
     * Migrated from COBOL: VALIDATE-CARD-DATA
     * Full business validation of card update data
     */
    public void validateCardData(CardUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        // Validate card status - COBOL: IF CDEMO-CARD-STATUS NOT IN ('0', '1', '2')
        if (request.getCardStatus() != null &&
                !request.getCardStatus().matches("^[012]$")) {
            errors.add("Card status must be 0 (Inactive), 1 (Active), or 2 (Suspended)");
        }

        // Validate expiration date - COBOL: VALIDATE-EXP-DATE
        if (request.getCardExpirationDate() != null) {
            if (request.getCardExpirationDate().isBefore(LocalDate.now())) {
                errors.add("Card expiration date cannot be in the past");
            }
            // Max 10 years in future - COBOL business rule
            if (request.getCardExpirationDate()
                    .isAfter(LocalDate.now().plusYears(10))) {
                errors.add("Card expiration date cannot be more than 10 years in the future");
            }
        }

        // Validate credit limit - COBOL: CDEMO-CREDIT-LIMIT NUMERIC check
        if (request.getCreditLimit() != null) {
            if (request.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
                errors.add("Credit limit cannot be negative");
            }
            if (request.getCreditLimit()
                    .compareTo(new BigDecimal("9999999999.99")) > 0) {
                errors.add("Credit limit exceeds maximum allowed value");
            }
        }

        // Validate cash limit does not exceed credit limit
        // COBOL: IF CDEMO-CASH-CREDIT-LIMIT > CDEMO-CREDIT-LIMIT
        if (request.getCashCreditLimit() != null && request.getCreditLimit() != null) {
            if (request.getCashCreditLimit()
                    .compareTo(request.getCreditLimit()) > 0) {
                errors.add("Cash credit limit cannot exceed credit limit");
            }
        }

        // Validate embossed name - COBOL: PIC X(50) alpha check
        if (request.getCardEmbossedName() != null &&
                request.getCardEmbossedName().length() > 50) {
            errors.add("Embossed name cannot exceed 50 characters");
        }

        // Validate open date not in future
        if (request.getOpenDate() != null &&
                request.getOpenDate().isAfter(LocalDate.now())) {
            errors.add("Open date cannot be in the future");
        }

        if (!errors.isEmpty()) {
            throw new CardUpdateException(RC_INVALID_CREDIT_LIMIT,
                    String.join("; ", errors));
        }
    }

    /**
     * Migrated from COBOL: COMPARE-OLD-NEW-RECORDS / UPD-CHECKS-OK
     * Checks if any fields actually changed to avoid unnecessary updates
     */
    public boolean hasChanges(CardUpdateRequest request,
                              com.carddemo.model.entity.CardData existingCard) {
        if (!request.getCardStatus().equals(existingCard.getCardStatus())) return true;
        if (isChanged(request.getCardEmbossedName(), existingCard.getCardEmbossedName()))
            return true;
        if (isChanged(request.getCardExpirationDate(),
                existingCard.getCardExpirationDate())) return true;
        if (isChanged(request.getCreditLimit(), existingCard.getCreditLimit())) return true;
        if (isChanged(request.getCashCreditLimit(), existingCard.getCashCreditLimit()))
            return true;
        if (isChanged(request.getGroupId(), existingCard.getGroupId())) return true;
        if (isChanged(request.getCardActiveStatus(), existingCard.getCardActiveStatus()))
            return true;
        return false;
    }

    /**
     * Luhn algorithm - card number validity check
     * Migrated from COBOL: CHECK-CARD-NUM paragraph
     */
    private boolean isValidLuhn(String cardNum) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNum.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(String.valueOf(cardNum.charAt(i)));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private boolean isChanged(Object newVal, Object oldVal) {
        if (newVal == null && oldVal == null) return false;
        if (newVal == null || oldVal == null) return true;
        return !newVal.equals(oldVal);
    }
}