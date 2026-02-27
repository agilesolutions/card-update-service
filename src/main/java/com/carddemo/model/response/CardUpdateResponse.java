package com.carddemo.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for card update operations
 * Maps to COBOL WS-CARD-FORUPD-REC output structure
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Credit Card Update Response")
public class CardUpdateResponse {

    @Schema(description = "Response status: SUCCESS or FAILURE")
    private String status;

    @Schema(description = "Response message")
    private String message;

    @Schema(description = "Response code mapped from COBOL WS-RETURN-CODE")
    private String returnCode;

    @Schema(description = "Updated card details")
    private CardDetails cardDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Card details in response")
    public static class CardDetails {
        private String cardNum;
        private Long custId;
        private String cardStatus;
        private String cardActiveStatus;
        private String cardEmbossedName;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate cardExpirationDate;

        private BigDecimal creditLimit;
        private BigDecimal cashCreditLimit;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate openDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;

        private String groupId;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdatedAt;
    }
}