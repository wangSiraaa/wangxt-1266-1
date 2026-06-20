package com.invoice.risk.controller;

import com.invoice.risk.dto.common.PageResult;
import com.invoice.risk.dto.common.Result;
import com.invoice.risk.dto.supplier.BlacklistRequest;
import com.invoice.risk.dto.supplier.SupplierImportRequest;
import com.invoice.risk.dto.supplier.SupplierQueryRequest;
import com.invoice.risk.dto.supplier.SupplierVO;
import com.invoice.risk.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "供应商管理", description = "供应商导入、查询、黑名单管理等相关接口")
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "导入供应商", description = "导入单条供应商信息")
    @PostMapping("/import")
    public Result<SupplierVO> importSupplier(@Valid @RequestBody SupplierImportRequest request) {
        return Result.success("供应商导入成功", supplierService.importSupplier(request));
    }

    @Operation(summary = "分页查询供应商", description = "根据条件分页查询供应商列表")
    @PostMapping("/query")
    public Result<PageResult<SupplierVO>> querySuppliers(@RequestBody SupplierQueryRequest request) {
        return Result.success(supplierService.querySuppliers(request));
    }

    @Operation(summary = "获取供应商详情", description = "根据ID获取供应商详细信息")
    @GetMapping("/{id}")
    public Result<SupplierVO> getSupplierById(@PathVariable Long id) {
        return Result.success(supplierService.getSupplierById(id));
    }

    @Operation(summary = "获取全部供应商", description = "获取全部供应商列表（用于下拉选择）")
    @GetMapping("/all")
    public Result<List<SupplierVO>> getAllSuppliers() {
        return Result.success(supplierService.getAllSuppliers());
    }

    @Operation(summary = "加入黑名单", description = "将供应商加入黑名单，自动冻结其关联发票报销")
    @PostMapping("/blacklist/add")
    public Result<Void> addToBlacklist(@Valid @RequestBody BlacklistRequest request) {
        supplierService.addToBlacklist(request);
        return Result.success("已加入黑名单", null);
    }

    @Operation(summary = "移出黑名单", description = "将供应商移出黑名单，自动解冻其关联发票报销")
    @PostMapping("/blacklist/remove")
    public Result<Void> removeFromBlacklist(@Valid @RequestBody BlacklistRequest request) {
        supplierService.removeFromBlacklist(request);
        return Result.success("已移出黑名单", null);
    }
}
