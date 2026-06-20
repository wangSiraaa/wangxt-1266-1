package com.invoice.risk.service;

import com.invoice.risk.context.UserContext;
import com.invoice.risk.dto.risk.ConfirmConclusionRequest;
import com.invoice.risk.dto.risk.MarkRiskRequest;
import com.invoice.risk.dto.risk.RiskMaterialVO;
import com.invoice.risk.dto.risk.RiskRecordVO;
import com.invoice.risk.dto.risk.SupplementMaterialRequest;
import com.invoice.risk.entity.ApprovalLog;
import com.invoice.risk.entity.Invoice;
import com.invoice.risk.entity.RiskMaterial;
import com.invoice.risk.entity.RiskRecord;
import com.invoice.risk.entity.SysUser;
import com.invoice.risk.enums.ApprovalActionEnum;
import com.invoice.risk.enums.InvoiceStatusEnum;
import com.invoice.risk.enums.MaterialTypeEnum;
import com.invoice.risk.enums.RiskTypeEnum;
import com.invoice.risk.enums.RoleEnum;
import com.invoice.risk.exception.BusinessException;
import com.invoice.risk.repository.ApprovalLogRepository;
import com.invoice.risk.repository.RiskMaterialRepository;
import com.invoice.risk.repository.RiskRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final InvoiceService invoiceService;
    private final RiskRecordRepository riskRecordRepository;
    private final RiskMaterialRepository riskMaterialRepository;
    private final ApprovalLogRepository approvalLogRepository;

    @Transactional
    public RiskRecordVO markRisk(MarkRiskRequest request) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser.getRole() != RoleEnum.TAX_SPECIALIST) {
            throw new BusinessException("只有税务专员可以标记风险");
        }

        Invoice invoice = invoiceService.getInvoiceEntityById(request.getInvoiceId());

        if (invoice.getStatus() == InvoiceStatusEnum.RESOLVED || invoice.getStatus() == InvoiceStatusEnum.REJECTED) {
            if (!invoice.getConclusionDeletable()) {
                throw new BusinessException("该发票已形成不可删除的处理结论，无法再标记风险");
            }
        }

        RiskRecord riskRecord = RiskRecord.builder()
                .invoiceId(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .riskType(request.getRiskType())
                .riskDescription(request.getRiskDescription())
                .markReason(request.getMarkReason())
                .markedBy(currentUser.getId())
                .markedByName(currentUser.getRealName())
                .isResolved(false)
                .build();
        RiskRecord saved = riskRecordRepository.save(riskRecord);

        invoice.setStatus(InvoiceStatusEnum.RISK_IDENTIFIED);
        if (request.getRiskType() == RiskTypeEnum.SUPPLIER_BLACKLIST) {
            invoice.setReimbursementFrozen(true);
        }
        invoiceService.saveInvoice(invoice);

        saveApprovalLog(invoice, ApprovalActionEnum.MARK_RISK,
                String.format("标记风险类型：%s，原因：%s",
                        request.getRiskType().getDescription(),
                        request.getMarkReason() != null ? request.getMarkReason() : "无"),
                currentUser);

        return toRiskRecordVO(saved);
    }

    @Transactional
    public RiskMaterialVO supplementMaterial(SupplementMaterialRequest request) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser.getRole() != RoleEnum.PROCUREMENT_HEAD) {
            throw new BusinessException("只有采购负责人可以补充材料");
        }

        Invoice invoice = invoiceService.getInvoiceEntityById(request.getInvoiceId());

        if (invoice.getStatus() != InvoiceStatusEnum.RISK_IDENTIFIED
                && invoice.getStatus() != InvoiceStatusEnum.MATERIALS_SUPPLEMENTED) {
            throw new BusinessException("当前发票状态不允许补充材料");
        }

        RiskMaterial material = RiskMaterial.builder()
                .invoiceId(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .materialType(request.getMaterialType())
                .materialName(request.getMaterialName())
                .materialUrl(request.getMaterialUrl())
                .contractNumber(request.getContractNumber())
                .contractDate(request.getContractDate())
                .deliveryNoteNumber(request.getDeliveryNoteNumber())
                .deliveryDate(request.getDeliveryDate())
                .remark(request.getRemark())
                .uploadedBy(currentUser.getId())
                .uploadedByName(currentUser.getRealName())
                .build();
        RiskMaterial saved = riskMaterialRepository.save(material);

        invoice.setStatus(InvoiceStatusEnum.MATERIALS_SUPPLEMENTED);
        invoiceService.saveInvoice(invoice);

        saveApprovalLog(invoice, ApprovalActionEnum.SUPPLEMENT_MATERIALS,
                String.format("补充材料类型：%s，名称：%s",
                        request.getMaterialType().getDescription(),
                        request.getMaterialName() != null ? request.getMaterialName() : "未命名"),
                currentUser);

        return toRiskMaterialVO(saved);
    }

    @Transactional
    public void confirmConclusion(ConfirmConclusionRequest request) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser.getRole() != RoleEnum.FINANCE_MANAGER) {
            throw new BusinessException("只有财务经理可以确认处理结论");
        }

        Invoice invoice = invoiceService.getInvoiceEntityById(request.getInvoiceId());

        if (invoice.getStatus() != InvoiceStatusEnum.MATERIALS_SUPPLEMENTED
                && invoice.getStatus() != InvoiceStatusEnum.RISK_IDENTIFIED
                && invoice.getStatus() != InvoiceStatusEnum.PENDING_CONFIRM
                && invoice.getStatus() != InvoiceStatusEnum.PENDING_REVIEW) {
            throw new BusinessException("当前发票状态不允许确认结论");
        }

        boolean hasContract = riskMaterialRepository.existsByInvoiceIdAndMaterialType(
                invoice.getId(), MaterialTypeEnum.CONTRACT);

        if (request.getResolved() && !hasContract) {
            throw new BusinessException("缺少采购合同，不能解除风险");
        }

        if (request.getResolved()) {
            invoice.setStatus(InvoiceStatusEnum.RESOLVED);
            List<RiskRecord> unresolvedRisks = riskRecordRepository.findByInvoiceIdAndIsResolvedFalse(invoice.getId());
            unresolvedRisks.forEach(r -> {
                r.setIsResolved(true);
                r.setResolveDescription(request.getConclusion());
                r.setResolvedBy(currentUser.getId());
                r.setResolvedByName(currentUser.getRealName());
                r.setResolvedAt(LocalDateTime.now());
            });
            riskRecordRepository.saveAll(unresolvedRisks);

            if (invoice.getReimbursementFrozen()) {
                invoice.setReimbursementFrozen(false);
            }

            saveApprovalLog(invoice, ApprovalActionEnum.CONFIRM_RESOLVED,
                    "解除风险：" + request.getConclusion(), currentUser);
        } else {
            invoice.setStatus(InvoiceStatusEnum.REJECTED);
            invoice.setReimbursementFrozen(true);
            saveApprovalLog(invoice, ApprovalActionEnum.CONFIRM_REJECTED,
                    "确认风险异常，冻结报销：" + request.getConclusion(), currentUser);
        }

        invoice.setConclusion(request.getConclusion());
        invoice.setConfirmedBy(currentUser.getId());
        invoice.setConfirmedByName(currentUser.getRealName());
        invoice.setConfirmedAt(LocalDateTime.now());
        invoice.setConclusionDeletable(false);

        invoiceService.saveInvoice(invoice);
    }

    @Transactional(readOnly = true)
    public List<RiskRecordVO> getRiskRecordsByInvoiceId(Long invoiceId) {
        return riskRecordRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream()
                .map(this::toRiskRecordVO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RiskMaterialVO> getMaterialsByInvoiceId(Long invoiceId) {
        return riskMaterialRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream()
                .map(this::toRiskMaterialVO)
                .collect(Collectors.toList());
    }

    private void saveApprovalLog(Invoice invoice, ApprovalActionEnum action, String remark, SysUser operator) {
        ApprovalLog log = ApprovalLog.builder()
                .invoiceId(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .action(action)
                .remark(remark)
                .operatorId(operator.getId())
                .operatorName(operator.getRealName())
                .operatorRole(operator.getRole().name())
                .build();
        approvalLogRepository.save(log);
    }

    private RiskRecordVO toRiskRecordVO(RiskRecord r) {
        return RiskRecordVO.builder()
                .id(r.getId())
                .invoiceId(r.getInvoiceId())
                .invoiceCode(r.getInvoiceCode())
                .riskType(r.getRiskType())
                .riskTypeDescription(r.getRiskType().getDescription())
                .riskDescription(r.getRiskDescription())
                .markReason(r.getMarkReason())
                .markedBy(r.getMarkedBy())
                .markedByName(r.getMarkedByName())
                .isResolved(r.getIsResolved())
                .resolveDescription(r.getResolveDescription())
                .resolvedByName(r.getResolvedByName())
                .resolvedAt(r.getResolvedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private RiskMaterialVO toRiskMaterialVO(RiskMaterial m) {
        return RiskMaterialVO.builder()
                .id(m.getId())
                .invoiceId(m.getInvoiceId())
                .invoiceCode(m.getInvoiceCode())
                .materialType(m.getMaterialType())
                .materialTypeDescription(m.getMaterialType().getDescription())
                .materialName(m.getMaterialName())
                .materialUrl(m.getMaterialUrl())
                .contractNumber(m.getContractNumber())
                .contractDate(m.getContractDate())
                .deliveryNoteNumber(m.getDeliveryNoteNumber())
                .deliveryDate(m.getDeliveryDate())
                .remark(m.getRemark())
                .uploadedByName(m.getUploadedByName())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
