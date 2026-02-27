-- ============================================================
-- Migration: V1__create_tables.sql
-- Description: Create tables mapped from COBOL VSAM files
--
-- COBOL VSAM → PostgreSQL mapping:
--   CARDDAT  → card_data table
--   CUSTDAT  → customer_data table
--   CARDAIX  → index on card_data(cust_id)
-- ============================================================

-- Card Data table
-- Mapped from COBOL: FD CARDDAT / 01 CARD-RECORD
CREATE TABLE IF NOT EXISTS card_data (
    id                  BIGSERIAL PRIMARY KEY,

    -- COBOL: CDEMO-CARD-NUM PIC X(16)
    card_num            VARCHAR(16) NOT NULL UNIQUE,

    -- COBOL: CDEMO-CUST-ID PIC 9(09)
    cust_id             BIGINT NOT NULL,

    -- COBOL: CDEMO-CARD-STATUS PIC X(01)
    -- Values: '0'=Inactive, '1'=Active, '2'=Suspended
    card_status         CHAR(1) NOT NULL DEFAULT '1'
                        CONSTRAINT chk_card_status CHECK (card_status IN ('0', '1', '2')),

    -- COBOL: CDEMO-CARD-ACTIVE-STATUS PIC X(01)
    card_active_status  CHAR(1) DEFAULT 'Y'
                        CONSTRAINT chk_active_status CHECK (card_active_status IN ('Y', 'N')),

    -- COBOL: CDEMO-CARD-EMBOSSED-NAME PIC X(50)
    card_embossed_name  VARCHAR(50),

    -- COBOL: CDEMO-CARD-EXPIRAION-DATE (typo preserved as comment)
    card_expiration_date DATE,

    -- COBOL: credit limit PIC S9(10)V99 COMP-3
    credit_limit        NUMERIC(15, 2) DEFAULT 0.00,

    -- COBOL: cash credit limit PIC S9(10)V99 COMP-3
    cash_credit_limit   NUMERIC(15, 2) DEFAULT 0.00,

    open_date           DATE,
    expiry_date         DATE,
    group_id            VARCHAR(10),

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT DEFAULT 0,

    CONSTRAINT fk_card_customer
        FOREIGN KEY (cust_id) REFERENCES customer_data(customer_id)
);

-- Customer Data table
-- Mapped from COBOL: FD CUSTDAT / 01 CUSTOMER-RECORD
CREATE TABLE IF NOT EXISTS customer_data (
    id                  BIGSERIAL PRIMARY KEY,

    -- COBOL: CDEMO-CUST-ID PIC 9(09)
    customer_id         BIGINT NOT NULL UNIQUE,

    -- COBOL: CDEMO-CUST-FIRST-NAME PIC X(25)
    first_name          VARCHAR(25),

    -- COBOL: CDEMO-CUST-MIDDLE-NAME PIC X(25)
    middle_name         VARCHAR(25),

    -- COBOL: CDEMO-CUST-LAST-NAME PIC X(25)
    last_name           VARCHAR(25),

    -- COBOL: CDEMO-CUST-ADDR-LINE-1 PIC X(50)
    addr_line1          VARCHAR(50),
    addr_line2          VARCHAR(50),
    addr_line3          VARCHAR(50),

    -- COBOL: CDEMO-CUST-STATE-CD PIC X(02)
    state_code          CHAR(2),

    -- COBOL: CDEMO-CUST-COUNTRY-CD PIC X(03)
    country_code        CHAR(3),

    -- COBOL: CDEMO-CUST-ZIP PIC X(10)
    zip_code            VARCHAR(10),

    -- COBOL: CDEMO-CUST-PHONE-NUM-1 PIC X(15)
    phone_num1          VARCHAR(15),
    phone_num2          VARCHAR(15),

    -- COBOL: CDEMO-CUST-SSN PIC 9(09)
    ssn                 BIGINT,

    -- COBOL: CDEMO-CUST-GOVT-ISSUED-ID PIC X(20)
    govt_issued_id      VARCHAR(20),

    -- COBOL: CDEMO-CUST-EFT-ACCOUNT-ID PIC X(10)
    eft_account_id      VARCHAR(10),

    -- COBOL: CDEMO-CUST-PRI-CARD-IND PIC X(01)
    primary_card_ind    CHAR(1),

    -- COBOL: CDEMO-CUST-FICO-CREDIT-SCORE PIC 9(03)
    fico_credit_score   SMALLINT,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes (replacing VSAM alternate indexes)
-- COBOL: CARDAIX - alternate index on CUSTID
CREATE INDEX IF NOT EXISTS idx_card_cust_id ON card_data(cust_id);
CREATE INDEX IF NOT EXISTS idx_card_num ON card_data(card_num);
CREATE INDEX IF NOT EXISTS idx_customer_id ON customer_data(customer_id);
CREATE INDEX IF NOT EXISTS idx_card_status ON card_data(card_status);

-- ============================================================
-- Seed test data
-- ============================================================
INSERT INTO customer_data (customer_id, first_name, last_name, state_code,
    country_code, zip_code, fico_credit_score)
VALUES
    (100000001, 'John', 'Smith', 'CA', 'USA', '90210', 750),
    (100000002, 'Jane', 'Doe', 'NY', 'USA', '10001', 720),
    (100000003, 'Bob', 'Johnson', 'TX', 'USA', '75001', 680);

INSERT INTO card_data (card_num, cust_id, card_status, card_active_status,
    card_embossed_name, card_expiration_date, credit_limit,
    cash_credit_limit, open_date, expiry_date, group_id)
VALUES
    ('4111111111111111', 100000001, '1', 'Y', 'JOHN SMITH',
     '2027-12-31', 5000.00, 1000.00, '2020-01-15', '2027-12-31', 'GRP001'),
    ('4222222222222222', 100000002, '1', 'Y', 'JANE DOE',
     '2026-06-30', 8000.00, 2000.00, '2021-03-01', '2026-06-30', 'GRP001'),
    ('4333333333333333', 100000003, '0', 'N', 'BOB JOHNSON',
     '2025-09-30', 3000.00, 500.00, '2019-07-20', '2025-09-30', 'GRP002');