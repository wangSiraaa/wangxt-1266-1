package com.invoice.risk.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    private long totalInvoices;
    private long normalInvoices;
    private long pendingReviewInvoices;
    private long riskIdentifiedInvoices;
    private long materialsSupplementedInvoices;
    private long pendingConfirmInvoices;
    private long resolvedInvoices;
    private long rejectedInvoices;
    private long frozenInvoices;
    private long totalSuppliers;
    private long blacklistedSuppliers;
    private long pendingRisks;
}
