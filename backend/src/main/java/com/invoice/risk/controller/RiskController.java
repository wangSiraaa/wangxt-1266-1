package com.invoice.risk.controller;

import com.invoice.risk.dto.common.Result;
import com.invoice.risk.dto.risk.ConfirmConclusionRequest;
import com.invoice.risk.dto.risk.MarkRiskRequest;
import com.invoice.risk.dto.risk.RiskMaterialVO;
import com.invoice.risk.dto.risk.RiskRecordVO;
import com.invoice.risk.dto.risk.SupplementMaterialRequest;
import com.invoice.risk.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "风险处理管理", description = "风险标记、材料补充、结论确认等相关接口")
@RestController
@RequestMapping("/risks")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @Operation(summary = "标记风险", description = "税务专员标记发票风险（需税务专员权限）")
    @PostMapping("/mark")
    public Result<RiskRecordVO> markRisk(@Valid @RequestBody MarkRiskRequest request) {
        return Result.success("风险标记成功", riskService.markRisk(request));
    }

    @Operation(summary = "补充材料", description = "采购负责人补充合同、收货单等材料（需采购负责人权限）")
    @PostMapping("/materials")
    public Result<RiskMaterialVO> supplementMaterial(@Valid @RequestBody SupplementMaterialRequest request) {
        return Result.success("材料补充成功", riskService.supplementMaterial(request));
    }

    @Operation(summary = "确认处理结论", description = "财务经理确认风险处理结论，形成不可删除结论（需财务经理权限）")
    @PostMapping("/confirm")
    public Result<Void> confirmConclusion(@Valid @RequestBody ConfirmConclusionRequest request) {
        riskService.confirmConclusion(request);
        return Result.success("处理结论确认成功", null);
    }

    @Operation(summary = "获取发票风险记录", description = "根据发票ID获取风险记录列表")
    @GetMapping("/invoices/{invoiceId}/records")
    public Result<List<RiskRecordVO>> getRiskRecords(@PathVariable Long invoiceId) {
        return Result.success(riskService.getRiskRecordsByInvoiceId(invoiceId));
    }

    @Operation(summary = "获取发票补充材料", description = "根据发票ID获取补充材料列表")
    @GetMapping("/invoices/{invoiceId}/materials")
    public Result<List<RiskMaterialVO>> getMaterials(@PathVariable Long invoiceId) {
        return Result.success(riskService.getMaterialsByInvoiceId(invoiceId));
    }
}
