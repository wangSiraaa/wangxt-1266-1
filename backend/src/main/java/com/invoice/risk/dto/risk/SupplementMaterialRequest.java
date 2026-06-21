package com.invoice.risk.dto.risk;

import com.invoice.risk.enums.MaterialTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplementMaterialRequest {

    @NotNull(message = "发票ID不能为空")
    private Long invoiceId;

    private Long materialId;

    @NotNull(message = "材料类型不能为空")
    private MaterialTypeEnum materialType;

    private String materialName;
    private String materialUrl;
    private String contractNumber;
    private LocalDate contractDate;
    private String deliveryNoteNumber;
    private LocalDate deliveryDate;
    private String remark;
}
