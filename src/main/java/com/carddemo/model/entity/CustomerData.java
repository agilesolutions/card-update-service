package com.carddemo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity mapped from COBOL CUSTOMER-RECORD structure
 * Original VSAM file: CUSTDAT
 */
@Entity
@Table(name = "customer_data",
        indexes = {
                @Index(name = "idx_customer_id", columnList = "customer_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CustomerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mapped from: CDEMO-CUST-ID (PIC 9(09))
     */
    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    /**
     * Mapped from: CDEMO-CUST-FIRST-NAME (PIC X(25))
     */
    @Column(name = "first_name", length = 25)
    private String firstName;

    /**
     * Mapped from: CDEMO-CUST-MIDDLE-NAME (PIC X(25))
     */
    @Column(name = "middle_name", length = 25)
    private String middleName;

    /**
     * Mapped from: CDEMO-CUST-LAST-NAME (PIC X(25))
     */
    @Column(name = "last_name", length = 25)
    private String lastName;

    /**
     * Mapped from: CDEMO-CUST-ADDR-LINE-1 (PIC X(50))
     */
    @Column(name = "addr_line1", length = 50)
    private String addrLine1;

    /**
     * Mapped from: CDEMO-CUST-ADDR-LINE-2 (PIC X(50))
     */
    @Column(name = "addr_line2", length = 50)
    private String addrLine2;

    /**
     * Mapped from: CDEMO-CUST-ADDR-LINE-3 (PIC X(50))
     */
    @Column(name = "addr_line3", length = 50)
    private String addrLine3;

    /**
     * Mapped from: CDEMO-CUST-STATE-CD (PIC X(02))
     */
    @Column(name = "state_code", length = 2)
    private String stateCode;

    /**
     * Mapped from: CDEMO-CUST-COUNTRY-CD (PIC X(03))
     */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    /**
     * Mapped from: CDEMO-CUST-ZIP (PIC X(10))
     */
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /**
     * Mapped from: CDEMO-CUST-PHONE-NUM-1 (PIC X(15))
     */
    @Column(name = "phone_num1", length = 15)
    private String phoneNum1;

    /**
     * Mapped from: CDEMO-CUST-PHONE-NUM-2 (PIC X(15))
     */
    @Column(name = "phone_num2", length = 15)
    private String phoneNum2;

    /**
     * Mapped from: CDEMO-CUST-SSN (PIC 9(09))
     */
    @Column(name = "ssn")
    private Long ssn;

    /**
     * Mapped from: CDEMO-CUST-GOVT-ISSUED-ID (PIC X(20))
     */
    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    /**
     * Mapped from: CDEMO-CUST-EFT-ACCOUNT-ID (PIC X(10))
     */
    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    /**
     * Mapped from: CDEMO-CUST-PRI-CARD-IND (PIC X(01))
     */
    @Column(name = "primary_card_ind", length = 1)
    private String primaryCardInd;

    /**
     * Mapped from: CDEMO-CUST-FICO-CREDIT-SCORE (PIC 9(03))
     */
    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}