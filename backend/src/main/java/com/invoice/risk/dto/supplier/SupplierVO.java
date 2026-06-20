package com.invoice.risk.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierVO {
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String taxNumber;
    private String address;
    private String contactPhone;
    private String bankName;
    private String bankAccount;
    private BigDecimal creditLimit;
    private Boolean isBlacklisted;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
