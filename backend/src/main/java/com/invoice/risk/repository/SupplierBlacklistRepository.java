package com.invoice.risk.repository;

import com.invoice.risk.entity.SupplierBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierBlacklistRepository extends JpaRepository<SupplierBlacklist, Long> {
    List<SupplierBlacklist> findBySupplierIdAndIsActiveTrue(Long supplierId);
    Optional<SupplierBlacklist> findTopBySupplierIdAndIsActiveTrueOrderByCreatedAtDesc(Long supplierId);
    boolean existsBySupplierIdAndIsActiveTrue(Long supplierId);
}
