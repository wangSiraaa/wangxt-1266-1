package com.invoice.risk.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierImportRequest {

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    @NotBlank(message = "税号不能为空")
    private String taxNumber;

    private String address;
    private String contactPhone;
    private String bankName;
    private String bankAccount;
    private BigDecimal creditLimit;
    private String remark;
}
