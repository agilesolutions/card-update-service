package com.carddemo.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO mapped from COBOL screen fields in COCRDUPC.cbl
 * Mirrors the WS-CARD-RID-CARDNUM and CCUP fields
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Credit Card Update Request")
public class CardUpdateRequest {

    /**
     * Mapped from: CDEMO-CARD-NUM / CC-CARD-NUM
     * COBOL: PIC X(16)
     */
    @Schema(description = "16-digit card number", example = "4111111111111111")
    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 16, message = "Card number must be exactly 16 characters")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must contain only digits")
    private String cardNum;

    /**
     * Mapped from: CDEMO-CUST-ID
     * COBOL: PIC 9(09)
     */
    @Schema(description = "Customer ID", example = "123456789")
    @NotNull(message = "Customer ID is required")
    @Min(value = 1, message = "Customer ID must be positive")
    @Max(value = 999999999, message = "Customer ID must be 9 digits or less")
    private Long custId;

    /**
     * Mapped from: CDEMO-CARD-STATUS
     * COBOL: PIC X(01) — '0'=Inactive, '1'=Active, '2'=Suspended
     */
    @Schema(description = "Card status: 0=Inactive, 1=Active, 2=Suspended", example = "1")
    @NotBlank(message = "Card status is required")
    @Pattern(regexp = "^[012]$", message = "Card status must be 0, 1, or 2")
    private String cardStatus;

    /**
     * Mapped from: CDEMO-CARD-EMBOSSED-NAME
     * COBOL: PIC X(50)
     */
    @Schema(description = "Name embossed on card", example = "JOHN DOE")
    @Size(max = 50, message = "Embossed name cannot exceed 50 characters")
    private String cardEmbossedName;

    /**
     * Mapped from: CDEMO-CARD-EXPIRAION-DATE (note: COBOL typo kept as comment)
     * COBOL: PIC X(10) — stored as YYYY-MM-DD
     */
    @Schema(description = "Card expiration date (YYYY-MM-DD)", example = "2027-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Card expiration date must be in the future")
    private LocalDate cardExpirationDate;

    /**
     * Mapped from credit limit fields
     * COBOL: PIC S9(10)V99 COMP-3
     */
    @Schema(description = "Credit limit", example = "5000.00")
    @DecimalMin(value = "0.00", message = "Credit limit must be non-negative")
    @DecimalMax(value = "9999999999.99", message = "Credit limit exceeds maximum value")
    @Digits(integer = 10, fraction = 2, message = "Credit limit format invalid")
    private BigDecimal creditLimit;

    /**
     * Mapped from cash credit limit
     * COBOL: PIC S9(10)V99 COMP-3
     */
    @Schema(description = "Cash credit limit", example = "1000.00")
    @DecimalMin(value = "0.00", message = "Cash credit limit must be non-negative")
    @DecimalMax(value = "9999999999.99", message = "Cash credit limit exceeds maximum value")
    @Digits(integer = 10, fraction = 2, message = "Cash credit limit format invalid")
    private BigDecimal cashCreditLimit;

    /**
     * Mapped from: open date
     * COBOL: PIC X(10)
     */
    @Schema(description = "Account open date (YYYY-MM-DD)", example = "2020-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate openDate;

    /**
     * Mapped from: expiry date YYYYMMDD
     * COBOL: PIC X(08)
     */
    @Schema(description = "Expiry date (YYYY-MM-DD)", example = "2027-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /**
     * Mapped from: group id
     * COBOL: PIC X(10)
     */
    @Schema(description = "Card group ID", example = "GRP001")
    @Size(max = 10, message = "Group ID cannot exceed 10 characters")
    private String groupId;

    /**
     * Mapped from: CDEMO-CARD-ACTIVE-STATUS
     * COBOL: PIC X(01)
     */
    @Schema(description = "Active status flag Y/N", example = "Y")
    @Pattern(regexp = "^[YN]$", message = "Active status must be Y or N")
    private String cardActiveStatus;
}