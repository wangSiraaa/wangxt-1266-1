package com.invoice.risk.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierQueryRequest {
    private String keyword;
    private Boolean isBlacklisted;
    private int pageNumber;
    private int pageSize;
}
