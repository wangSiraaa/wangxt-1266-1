package com.invoice.risk.dto.risk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmConclusionRequest {

    @NotNull(message = "发票ID不能为空")
    private Long invoiceId;

    @NotNull(message = "是否解除风险不能为空")
    private Boolean resolved;

    @NotBlank(message = "处理结论不能为空")
    private String conclusion;
}
