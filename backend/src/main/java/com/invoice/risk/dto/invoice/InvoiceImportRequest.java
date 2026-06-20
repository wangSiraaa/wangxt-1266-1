package com.invoice.risk.dto.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceImportRequest {

    @NotBlank(message = "发票代码不能为空")
    private String invoiceCode;

    @NotBlank(message = "发票号码不能为空")
    private String invoiceNumber;

    @NotBlank(message = "发票类型不能为空")
    private String invoiceType;

    @NotNull(message = "开票日期不能为空")
    private LocalDate invoiceDate;

    @NotNull(message = "税前金额不能为空")
    private BigDecimal amountBeforeTax;

    @NotNull(message = "税额不能为空")
    private BigDecimal taxAmount;

    @NotNull(message = "价税合计不能为空")
    private BigDecimal totalAmount;

    private BigDecimal taxRate;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String buyerTaxNumber;
    private String buyerName;
    private String goodsDescription;
    private String drawer;
    private String payee;
    private String checker;
    private String remark;
}
