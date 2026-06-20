package com.invoice.risk.dto.invoice;

import com.invoice.risk.enums.InvoiceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceVO {
    private Long id;
    private String invoiceCode;
    private String invoiceNumber;
    private String invoiceType;
    private LocalDate invoiceDate;
    private BigDecimal amountBeforeTax;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal taxRate;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private String buyerTaxNumber;
    private String buyerName;
    private String goodsDescription;
    private String remark;
    private InvoiceStatusEnum status;
    private String statusDescription;
    private Boolean reimbursementFrozen;
    private String conclusion;
    private String confirmedByName;
    private LocalDateTime confirmedAt;
    private Boolean conclusionDeletable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
