package com.invoice.risk.service;

import com.invoice.risk.context.UserContext;
import com.invoice.risk.dto.common.PageResult;
import com.invoice.risk.dto.supplier.BlacklistRequest;
import com.invoice.risk.dto.supplier.SupplierImportRequest;
import com.invoice.risk.dto.supplier.SupplierQueryRequest;
import com.invoice.risk.dto.supplier.SupplierVO;
import com.invoice.risk.entity.ApprovalLog;
import com.invoice.risk.entity.Invoice;
import com.invoice.risk.entity.Supplier;
import com.invoice.risk.entity.SupplierBlacklist;
import com.invoice.risk.entity.SysUser;
import com.invoice.risk.enums.ApprovalActionEnum;
import com.invoice.risk.enums.InvoiceStatusEnum;
import com.invoice.risk.exception.BusinessException;
import com.invoice.risk.repository.ApprovalLogRepository;
import com.invoice.risk.repository.InvoiceRepository;
import com.invoice.risk.repository.SupplierBlacklistRepository;
import com.invoice.risk.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierBlacklistRepository supplierBlacklistRepository;
    private final InvoiceRepository invoiceRepository;
    private final ApprovalLogRepository approvalLogRepository;

    @Transactional
    public SupplierVO importSupplier(SupplierImportRequest request) {
        if (supplierRepository.existsBySupplierCode(request.getSupplierCode())) {
            throw new BusinessException("供应商编码已存在");
        }

        Supplier supplier = Supplier.builder()
                .supplierCode(request.getSupplierCode())
                .supplierName(request.getSupplierName())
                .taxNumber(request.getTaxNumber())
                .address(request.getAddress())
                .contactPhone(request.getContactPhone())
                .bankName(request.getBankName())
                .bankAccount(request.getBankAccount())
                .creditLimit(request.getCreditLimit())
                .isBlacklisted(false)
                .remark(request.getRemark())
                .build();

        Supplier saved = supplierRepository.save(supplier);
        return toVO(saved);
    }

    @Transactional(readOnly = true)
    public PageResult<SupplierVO> querySuppliers(SupplierQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPageNumber(),
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Supplier> page = supplierRepository.findByConditions(
                request.getKeyword(),
                request.getIsBlacklisted(),
                pageable
        );

        List<SupplierVO> content = page.getContent().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.<SupplierVO>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public SupplierVO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        return toVO(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierVO> getAllSuppliers() {
        return supplierRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToBlacklist(BlacklistRequest request) {
        SysUser currentUser = UserContext.getUser();

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        if (supplier.getIsBlacklisted()) {
            throw new BusinessException("供应商已在黑名单中");
        }

        SupplierBlacklist blacklist = SupplierBlacklist.builder()
                .supplierId(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .reason(request.getReason())
                .operatorId(currentUser.getId())
                .operatorName(currentUser.getRealName())
                .isActive(true)
                .build();
        supplierBlacklistRepository.save(blacklist);

        supplier.setIsBlacklisted(true);
        supplierRepository.save(supplier);

        List<Invoice> invoices = invoiceRepository.findBySupplierId(supplier.getId());
        invoices.forEach(inv -> {
            if (inv.getStatus() != InvoiceStatusEnum.RESOLVED
                    && inv.getStatus() != InvoiceStatusEnum.REJECTED) {
                inv.setReimbursementFrozen(true);
                if (inv.getStatus() == InvoiceStatusEnum.NORMAL) {
                    inv.setStatus(InvoiceStatusEnum.PENDING_REVIEW);
                }
                ApprovalLog log = ApprovalLog.builder()
                        .invoiceId(inv.getId())
                        .invoiceCode(inv.getInvoiceCode())
                        .action(ApprovalActionEnum.ADD_BLACKLIST)
                        .remark("供应商加入黑名单，冻结报销：" + request.getReason())
                        .operatorId(currentUser.getId())
                        .operatorName(currentUser.getRealName())
                        .operatorRole(currentUser.getRole().name())
                        .build();
                approvalLogRepository.save(log);
            }
        });
        invoiceRepository.saveAll(invoices);
    }

    @Transactional
    public void removeFromBlacklist(BlacklistRequest request) {
        SysUser currentUser = UserContext.getUser();

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        if (!supplier.getIsBlacklisted()) {
            throw new BusinessException("供应商不在黑名单中");
        }

        SupplierBlacklist blacklist = supplierBlacklistRepository
                .findTopBySupplierIdAndIsActiveTrueOrderByCreatedAtDesc(supplier.getId())
                .orElseThrow(() -> new BusinessException("黑名单记录不存在"));

        blacklist.setIsActive(false);
        blacklist.setRemovedAt(LocalDateTime.now());
        blacklist.setRemovedBy(currentUser.getId());
        blacklist.setRemoveReason(request.getReason());
        supplierBlacklistRepository.save(blacklist);

        supplier.setIsBlacklisted(false);
        supplierRepository.save(supplier);

        List<Invoice> invoices = invoiceRepository.findBySupplierId(supplier.getId());
        invoices.forEach(inv -> {
            if (inv.getStatus() != InvoiceStatusEnum.REJECTED) {
                inv.setReimbursementFrozen(false);
                ApprovalLog log = ApprovalLog.builder()
                        .invoiceId(inv.getId())
                        .invoiceCode(inv.getInvoiceCode())
                        .action(ApprovalActionEnum.REMOVE_BLACKLIST)
                        .remark("供应商移出黑名单，解冻报销：" + request.getReason())
                        .operatorId(currentUser.getId())
                        .operatorName(currentUser.getRealName())
                        .operatorRole(currentUser.getRole().name())
                        .build();
                approvalLogRepository.save(log);
            }
        });
        invoiceRepository.saveAll(invoices);
    }

    private SupplierVO toVO(Supplier s) {
        return SupplierVO.builder()
                .id(s.getId())
                .supplierCode(s.getSupplierCode())
                .supplierName(s.getSupplierName())
                .taxNumber(s.getTaxNumber())
                .address(s.getAddress())
                .contactPhone(s.getContactPhone())
                .bankName(s.getBankName())
                .bankAccount(s.getBankAccount())
                .creditLimit(s.getCreditLimit())
                .isBlacklisted(s.getIsBlacklisted())
                .remark(s.getRemark())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
