package com.invoice.risk.repository;

import com.invoice.risk.entity.Invoice;
import com.invoice.risk.enums.InvoiceStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceCode(String invoiceCode);
    boolean existsByInvoiceCode(String invoiceCode);
    List<Invoice> findBySupplierId(Long supplierId);
    long countByStatus(InvoiceStatusEnum status);

    @Query("SELECT i FROM Invoice i WHERE " +
           "(:invoiceCode IS NULL OR i.invoiceCode LIKE %:invoiceCode%) AND " +
           "(:supplierName IS NULL OR i.supplierName LIKE %:supplierName%) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:startDate IS NULL OR i.invoiceDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.invoiceDate <= :endDate) AND " +
           "(:reimbursementFrozen IS NULL OR i.reimbursementFrozen = :reimbursementFrozen)")
    Page<Invoice> findByConditions(
            @Param("invoiceCode") String invoiceCode,
            @Param("supplierName") String supplierName,
            @Param("status") InvoiceStatusEnum status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("reimbursementFrozen") Boolean reimbursementFrozen,
            Pageable pageable);
}
