package com.invoice.risk.controller;

import com.invoice.risk.dto.approval.ApprovalLogVO;
import com.invoice.risk.dto.common.PageResult;
import com.invoice.risk.dto.common.Result;
import com.invoice.risk.service.ApprovalLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "审批日志管理", description = "审批操作日志查询相关接口")
@RestController
@RequestMapping("/approval-logs")
@RequiredArgsConstructor
public class ApprovalLogController {

    private final ApprovalLogService approvalLogService;

    @Operation(summary = "获取发票审批日志", description = "根据发票ID获取审批操作日志列表")
    @GetMapping("/invoices/{invoiceId}")
    public Result<List<ApprovalLogVO>> getLogsByInvoiceId(@PathVariable Long invoiceId) {
        return Result.success(approvalLogService.getLogsByInvoiceId(invoiceId));
    }

    @Operation(summary = "分页查询审批日志", description = "根据条件分页查询全部审批日志")
    @GetMapping("/query")
    public Result<PageResult<ApprovalLogVO>> queryLogs(
            @RequestParam(required = false) String invoiceCode,
            @RequestParam(required = false) String operatorName,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(approvalLogService.queryLogs(invoiceCode, operatorName, pageNumber, pageSize));
    }
}
