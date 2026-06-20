package com.invoice.risk.dto.supplier;

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
public class BlacklistRequest {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "原因不能为空")
    private String reason;
}
