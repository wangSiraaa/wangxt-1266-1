package com.invoice.risk.controller;

import com.invoice.risk.dto.common.PageResult;
import com.invoice.risk.dto.common.Result;
import com.invoice.risk.dto.invoice.InvoiceImportRequest;
import com.invoice.risk.dto.invoice.InvoiceQueryRequest;
import com.invoice.risk.dto.invoice.InvoiceVO;
import com.invoice.risk.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "发票管理", description = "发票导入、查询等相关接口")
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "导入发票", description = "导入单张发票信息")
    @PostMapping("/import")
    public Result<InvoiceVO> importInvoice(@Valid @RequestBody InvoiceImportRequest request) {
        return Result.success("发票导入成功", invoiceService.importInvoice(request));
    }

    @Operation(summary = "分页查询发票", description = "根据条件分页查询发票列表")
    @PostMapping("/query")
    public Result<PageResult<InvoiceVO>> queryInvoices(@RequestBody InvoiceQueryRequest request) {
        return Result.success(invoiceService.queryInvoices(request));
    }

    @Operation(summary = "获取发票详情", description = "根据ID获取发票详细信息")
    @GetMapping("/{id}")
    public Result<InvoiceVO> getInvoiceById(@PathVariable Long id) {
        return Result.success(invoiceService.getInvoiceById(id));
    }
}
