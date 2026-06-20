package com.invoice.risk.controller;

import com.invoice.risk.dto.common.PageResult;
import com.invoice.risk.dto.common.Result;
import com.invoice.risk.dto.dashboard.DashboardVO;
import com.invoice.risk.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仪表盘管理", description = "首页数据统计相关接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘数据", description = "获取发票、供应商、风险的统计数据")
    @GetMapping
    public Result<DashboardVO> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}
