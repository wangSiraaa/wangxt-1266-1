package com.invoice.risk.dto.risk;

import com.invoice.risk.enums.RiskTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkRiskRequest {

    @NotNull(message = "发票ID不能为空")
    private Long invoiceId;

    @NotNull(message = "风险类型不能为空")
    private RiskTypeEnum riskType;

    private String riskDescription;

    private String markReason;
}
