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

        if (request.getRequiredMaterials() != null && !request.getRequiredMaterials().isEmpty()) {
            for (MaterialTypeEnum materialType : request.getRequiredMaterials()) {
                boolean exists = riskMaterialRepository.existsByInvoiceIdAndMaterialTypeAndMaterialStatus(
                        invoice.getId(), materialType, MaterialStatusEnum.PENDING);
                if (!exists) {
                    RiskMaterial pendingMaterial = RiskMaterial.builder()
                            .invoiceId(invoice.getId())
                            .invoiceCode(invoice.getInvoiceCode())
                            .materialType(materialType)
                            .materialStatus(MaterialStatusEnum.PENDING)
                            .build();
                    riskMaterialRepository.save(pendingMaterial);
                }
            }
        }

        String materialDesc = "";
        if (request.getRequiredMaterials() != null && !request.getRequiredMaterials().isEmpty()) {
            materialDesc = String.format("，要求补充材料：%s",
                    request.getRequiredMaterials().stream()
                            .map(MaterialTypeEnum::getDescription)
                            .collect(java.util.stream.Collectors.joining("、")));
        }

        saveApprovalLog(invoice, ApprovalActionEnum.MARK_RISK,
                String.format("标记风险类型：%s，原因：%s%s",
                        request.getRiskType().getDescription(),
                        request.getMarkReason() != null ? request.getMarkReason() : "无",
                        materialDesc),
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

        RiskMaterial material;
        if (request.getMaterialId() != null) {
            material = riskMaterialRepository.findById(request.getMaterialId())
                    .orElseThrow(() -> new BusinessException("材料记录不存在"));
            if (material.getMaterialStatus() == MaterialStatusEnum.SUPPLEMENTED) {
                throw new BusinessException("该材料已补充，请勿重复上传");
            }
            material.setMaterialType(request.getMaterialType());
            material.setMaterialStatus(MaterialStatusEnum.SUPPLEMENTED);
        } else {
            material = RiskMaterial.builder()
                    .invoiceId(invoice.getId())
                    .invoiceCode(invoice.getInvoiceCode())
                    .materialType(request.getMaterialType())
                    .materialStatus(MaterialStatusEnum.SUPPLEMENTED)
                    .build();
        }

        material.setMaterialName(request.getMaterialName());
        material.setMaterialUrl(request.getMaterialUrl());
        material.setContractNumber(request.getContractNumber());
        material.setContractDate(request.getContractDate());
        material.setDeliveryNoteNumber(request.getDeliveryNoteNumber());
        material.setDeliveryDate(request.getDeliveryDate());
        material.setRemark(request.getRemark());
        material.setUploadedBy(currentUser.getId());
        material.setUploadedByName(currentUser.getRealName());
        material.setUploadedAt(LocalDateTime.now());

        RiskMaterial saved = riskMaterialRepository.save(material);

        boolean allSupplemented = riskMaterialRepository
                .findByInvoiceIdAndMaterialStatus(invoice.getId(), MaterialStatusEnum.PENDING)
                .isEmpty();

        if (allSupplemented) {
            invoice.setStatus(InvoiceStatusEnum.MATERIALS_SUPPLEMENTED);
            invoiceService.saveInvoice(invoice);
        }

        saveApprovalLog(invoice, ApprovalActionEnum.SUPPLEMENT_MATERIALS,
                String.format("补充材料类型：%s，名称：%s",
                        request.getMaterialType().getDescription(),
                        request.getMaterialName() != null ? request.getMaterialName() : "未命名"),
                currentUser);

        return toRiskMaterialVO(saved);
    }

    @Transactional
    public RiskMaterialVO createPendingMaterial(SupplementMaterialRequest request) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser.getRole() != RoleEnum.TAX_SPECIALIST) {
            throw new BusinessException("只有税务专员可以添加待补充材料");
        }

        Invoice invoice = invoiceService.getInvoiceEntityById(request.getInvoiceId());

        boolean exists = riskMaterialRepository.existsByInvoiceIdAndMaterialTypeAndMaterialStatus(
                invoice.getId(), request.getMaterialType(), MaterialStatusEnum.PENDING);
        if (exists) {
            throw new BusinessException("该类型材料已在待补充清单中");
        }

        RiskMaterial material = RiskMaterial.builder()
                .invoiceId(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .materialType(request.getMaterialType())
                .materialStatus(MaterialStatusEnum.PENDING)
                .build();
        RiskMaterial saved = riskMaterialRepository.save(material);

        saveApprovalLog(invoice, ApprovalActionEnum.MARK_RISK,
                String.format("新增待补充材料：%s", request.getMaterialType().getDescription()),
                currentUser);

        return toRiskMaterialVO(saved);
    }

    @Transactional
    public void deletePendingMaterial(Long materialId) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser.getRole() != RoleEnum.TAX_SPECIALIST) {
            throw new BusinessException("只有税务专员可以删除待补充材料");
        }

        RiskMaterial material = riskMaterialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException("材料记录不存在"));

        if (material.getMaterialStatus() == MaterialStatusEnum.SUPPLEMENTED) {
            throw new BusinessException("已补充的材料不能删除");
        }

        Invoice invoice = invoiceService.getInvoiceEntityById(material.getInvoiceId());
        if (!invoice.getConclusionDeletable()) {
            throw new BusinessException("该发票已形成不可删除的处理结论，无法删除材料");
        }

        riskMaterialRepository.delete(material);

        saveApprovalLog(invoice, ApprovalActionEnum.MARK_RISK,
                String.format("删除待补充材料：%s", material.getMaterialType().getDescription()),
                currentUser);
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

        boolean hasContract = riskMaterialRepository.existsByInvoiceIdAndMaterialTypeAndMaterialStatus(
                invoice.getId(), MaterialTypeEnum.CONTRACT, MaterialStatusEnum.SUPPLEMENTED);

        if (request.getResolved() && !hasContract) {
            throw new BusinessException("缺少已上传的采购合同，不能解除风险");
        }

        List<RiskMaterial> pendingMaterials = riskMaterialRepository
                .findByInvoiceIdAndMaterialStatus(invoice.getId(), MaterialStatusEnum.PENDING);

        if (request.getResolved() && !pendingMaterials.isEmpty()) {
            String pendingNames = pendingMaterials.stream()
                    .map(m -> m.getMaterialType().getDescription())
                    .collect(java.util.stream.Collectors.joining("、"));
            throw new BusinessException("还有待补充的材料未上传：" + pendingNames + "，请先完成所有材料补充");
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
                .materialStatus(m.getMaterialStatus())
                .materialStatusDescription(m.getMaterialStatus().getDescription())
                .materialName(m.getMaterialName())
                .materialUrl(m.getMaterialUrl())
                .contractNumber(m.getContractNumber())
                .contractDate(m.getContractDate())
                .deliveryNoteNumber(m.getDeliveryNoteNumber())
                .deliveryDate(m.getDeliveryDate())
                .remark(m.getRemark())
                .uploadedByName(m.getUploadedByName())
                .uploadedAt(m.getUploadedAt())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
