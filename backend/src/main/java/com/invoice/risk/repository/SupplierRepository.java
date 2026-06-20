package com.invoice.risk.repository;

import com.invoice.risk.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    Optional<Supplier> findByTaxNumber(String taxNumber);
    boolean existsBySupplierCode(String supplierCode);

    @Query("SELECT s FROM Supplier s WHERE " +
           "(:keyword IS NULL OR s.supplierName LIKE %:keyword% OR s.supplierCode LIKE %:keyword%) AND " +
           "(:isBlacklisted IS NULL OR s.isBlacklisted = :isBlacklisted)")
    Page<Supplier> findByConditions(
            @Param("keyword") String keyword,
            @Param("isBlacklisted") Boolean isBlacklisted,
            Pageable pageable);
}
