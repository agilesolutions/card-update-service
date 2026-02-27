package com.carddemo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity mapped from COBOL CARD-RECORD structure in COCRDUPC.cbl
 * Original VSAM file: CARDDAT / CARDAIX
 */
@Entity
@Table(name = "card_data",
        indexes = {
                @Index(name = "idx_card_num", columnList = "card_num"),
                @Index(name = "idx_cust_id", columnList = "cust_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CardData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mapped from: CDEMO-CARD-NUM (PIC X(16))
     */
    @Column(name = "card_num", length = 16, nullable = false, unique = true)
    private String cardNum;

    /**
     * Mapped from: CDEMO-CUST-ID (PIC 9(09))
     */
    @Column(name = "cust_id", nullable = false)
    private Long custId;

    /**
     * Mapped from: CDEMO-CARD-STATUS (PIC X(01))
     * Values: '0' = Inactive, '1' = Active, '2' = Suspended
     */
    @Column(name = "card_status", length = 1, nullable = false)
    private String cardStatus;

    /**
     * Mapped from: CDEMO-CARD-ACTIVE-STATUS (PIC X(01))
     */
    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    /**
     * Mapped from: CDEMO-CARD-EMBOSSED-NAME (PIC X(50))
     */
    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    /**
     * Mapped from: CDEMO-CARD-EXPIRAION-DATE (PIC X(10))
     */
    @Column(name = "card_expiration_date")
    private LocalDate cardExpirationDate;

    /**
     * Mapped from credit limit fields
     */
    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    /**
     * Mapped from cash credit limit fields
     */
    @Column(name = "cash_credit_limit", precision = 15, scale = 2)
    private BigDecimal cashCreditLimit;

    /**
     * Mapped from open date
     */
    @Column(name = "open_date")
    private LocalDate openDate;

    /**
     * Mapped from expiry date - YYYYMMDD format in COBOL
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * Mapped from group id
     */
    @Column(name = "group_id", length = 10)
    private String groupId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}