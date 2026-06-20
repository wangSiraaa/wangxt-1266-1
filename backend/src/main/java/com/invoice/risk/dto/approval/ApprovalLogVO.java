package com.invoice.risk.dto.approval;

import com.invoice.risk.enums.ApprovalActionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalLogVO {
    private Long id;
    private Long invoiceId;
    private String invoiceCode;
    private ApprovalActionEnum action;
    private String actionDescription;
    private String remark;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String operatorRoleDescription;
    private LocalDateTime createdAt;
}
