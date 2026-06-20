package com.invoice.risk.dto.invoice;

import com.invoice.risk.enums.InvoiceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceQueryRequest {
    private String invoiceCode;
    private String supplierName;
    private InvoiceStatusEnum status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean reimbursementFrozen;
    private int pageNumber;
    private int pageSize;
}
