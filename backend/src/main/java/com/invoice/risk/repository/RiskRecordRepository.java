package com.invoice.risk.repository;

import com.invoice.risk.entity.RiskRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRecordRepository extends JpaRepository<RiskRecord, Long> {
    List<RiskRecord> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);
    List<RiskRecord> findByInvoiceIdAndIsResolvedFalse(Long invoiceId);
    long countByIsResolvedFalse();
}
