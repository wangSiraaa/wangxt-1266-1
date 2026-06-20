package com.invoice.risk.service;

import com.invoice.risk.dto.dashboard.DashboardVO;
import com.invoice.risk.enums.InvoiceStatusEnum;
import com.invoice.risk.repository.InvoiceRepository;
import com.invoice.risk.repository.RiskRecordRepository;
import com.invoice.risk.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;
    private final RiskRecordRepository riskRecordRepository;

    @Transactional(readOnly = true)
    public DashboardVO getDashboard() {
        long totalInvoices = invoiceRepository.count();
        long normalInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.NORMAL);
        long pendingReviewInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.PENDING_REVIEW);
        long riskIdentifiedInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.RISK_IDENTIFIED);
        long materialsSupplementedInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.MATERIALS_SUPPLEMENTED);
        long pendingConfirmInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.PENDING_CONFIRM);
        long resolvedInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.RESOLVED);
        long rejectedInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.REJECTED);
        long frozenInvoices = invoiceRepository.countByStatus(InvoiceStatusEnum.FROZEN);

        long totalSuppliers = supplierRepository.count();
        long blacklistedSuppliers = supplierRepository.count();
        blacklistedSuppliers = supplierRepository.findByConditions(null, true, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();

        long pendingRisks = riskRecordRepository.countByIsResolvedFalse();

        return DashboardVO.builder()
                .totalInvoices(totalInvoices)
                .normalInvoices(normalInvoices)
                .pendingReviewInvoices(pendingReviewInvoices)
                .riskIdentifiedInvoices(riskIdentifiedInvoices)
                .materialsSupplementedInvoices(materialsSupplementedInvoices)
                .pendingConfirmInvoices(pendingConfirmInvoices)
                .resolvedInvoices(resolvedInvoices)
                .rejectedInvoices(rejectedInvoices)
                .frozenInvoices(frozenInvoices)
                .totalSuppliers(totalSuppliers)
                .blacklistedSuppliers(blacklistedSuppliers)
                .pendingRisks(pendingRisks)
                .build();
    }
}
