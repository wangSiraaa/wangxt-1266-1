package com.invoice.risk.dto.risk;

import com.invoice.risk.enums.RiskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRecordVO {
    private Long id;
    private Long invoiceId;
    private String invoiceCode;
    private RiskTypeEnum riskType;
    private String riskTypeDescription;
    private String riskDescription;
    private String markReason;
    private Long markedBy;
    private String markedByName;
    private Boolean isResolved;
    private String resolveDescription;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
