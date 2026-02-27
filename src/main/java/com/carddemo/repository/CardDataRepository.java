package com.carddemo.repository;

import com.carddemo.model.entity.CardData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CardData entity
 * Replaces COBOL VSAM file operations:
 *  - READ CARDDAT -> findByCardNum()
 *  - REWRITE CARDDAT -> save()
 *  - READ CARDAIX (alternate index by cust_id) -> findByCustId()
 */
@Repository
public interface CardDataRepository extends JpaRepository<CardData, Long>,
        JpaSpecificationExecutor<CardData> {

    /**
     * Replaces: READ CARDDAT RECORD INTO WS-CARD-RID
     * COBOL: READ CARDDAT INTO WS-CARD-RID-CARDNUM
     */
    Optional<CardData> findByCardNum(String cardNum);

    /**
     * Replaces: READ CARDAIX (alternate index by customer ID)
     */
    List<CardData> findByCustId(Long custId);

    /**
     * Check if card exists - replaces COBOL NOTFND condition check
     */
    boolean existsByCardNum(String cardNum);

    /**
     * Replaces: READ CARDAIX BY ALTERNATE KEY cust_id
     * Find active cards by customer
     */
    @Query("SELECT c FROM CardData c WHERE c.custId = :custId AND c.cardStatus = '1'")
    List<CardData> findActiveCardsByCustId(@Param("custId") Long custId);

    /**
     * Bulk status update - optimized for batch operations
     */
    @Modifying
    @Query("UPDATE CardData c SET c.cardStatus = :status WHERE c.custId = :custId")
    int updateCardStatusByCustId(@Param("custId") Long custId,
                                 @Param("status") String status);
}