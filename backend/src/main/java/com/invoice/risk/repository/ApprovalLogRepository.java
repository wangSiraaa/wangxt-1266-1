package com.invoice.risk.repository;

import com.invoice.risk.entity.ApprovalLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalLogRepository extends JpaRepository<ApprovalLog, Long> {
    List<ApprovalLog> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);

    @Query("SELECT a FROM ApprovalLog a WHERE " +
           "(:invoiceCode IS NULL OR a.invoiceCode LIKE %:invoiceCode%) AND " +
           "(:operatorName IS NULL OR a.operatorName LIKE %:operatorName%)")
    Page<ApprovalLog> findByConditions(
            @Param("invoiceCode") String invoiceCode,
            @Param("operatorName") String operatorName,
            Pageable pageable);
}
