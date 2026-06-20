package com.invoice.risk.dto.risk;

import com.invoice.risk.enums.MaterialTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskMaterialVO {
    private Long id;
    private Long invoiceId;
    private String invoiceCode;
    private MaterialTypeEnum materialType;
    private String materialTypeDescription;
    private String materialName;
    private String materialUrl;
    private String contractNumber;
    private LocalDate contractDate;
    private String deliveryNoteNumber;
    private LocalDate deliveryDate;
    private String remark;
    private String uploadedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
