package com.invoice.risk.service;

import com.invoice.risk.dto.approval.ApprovalLogVO;
import com.invoice.risk.dto.common.PageResult;
import com.invoice.risk.entity.ApprovalLog;
import com.invoice.risk.enums.ApprovalActionEnum;
import com.invoice.risk.enums.RoleEnum;
import com.invoice.risk.repository.ApprovalLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalLogService {

    private final ApprovalLogRepository approvalLogRepository;

    @Transactional(readOnly = true)
    public List<ApprovalLogVO> getLogsByInvoiceId(Long invoiceId) {
        return approvalLogRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResult<ApprovalLogVO> queryLogs(String invoiceCode, String operatorName,
                                                int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ApprovalLog> page = approvalLogRepository.findByConditions(invoiceCode, operatorName, pageable);

        List<ApprovalLogVO> content = page.getContent().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.<ApprovalLogVO>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    private ApprovalLogVO toVO(ApprovalLog log) {
        RoleEnum role = null;
        String roleDesc = "";
        try {
            role = RoleEnum.valueOf(log.getOperatorRole());
            roleDesc = role.getDescription();
        } catch (Exception ignored) {}

        return ApprovalLogVO.builder()
                .id(log.getId())
                .invoiceId(log.getInvoiceId())
                .invoiceCode(log.getInvoiceCode())
                .action(log.getAction())
                .actionDescription(log.getAction().getDescription())
                .remark(log.getRemark())
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .operatorRole(log.getOperatorRole())
                .operatorRoleDescription(roleDesc)
                .createdAt(log.getCreatedAt())
                .build();
    }
}
