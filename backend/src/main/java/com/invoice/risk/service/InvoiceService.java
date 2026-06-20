package com.invoice.risk.service;

import com.invoice.risk.context.UserContext;
import com.invoice.risk.dto.common.PageResult;
import com.invoice.risk.dto.invoice.InvoiceImportRequest;
import com.invoice.risk.dto.invoice.InvoiceQueryRequest;
import com.invoice.risk.dto.invoice.InvoiceVO;
import com.invoice.risk.entity.Invoice;
import com.invoice.risk.entity.Supplier;
import com.invoice.risk.entity.SupplierBlacklist;
import com.invoice.risk.enums.InvoiceStatusEnum;
import com.invoice.risk.exception.BusinessException;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierBlacklistRepository supplierBlacklistRepository;

    @Transactional
    public InvoiceVO importInvoice(InvoiceImportRequest request) {
        if (invoiceRepository.existsByInvoiceCode(request.getInvoiceCode())) {
            throw new BusinessException("发票代码已存在");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new BusinessException("供应商不存在"));

        Invoice invoice = Invoice.builder()
                .invoiceCode(request.getInvoiceCode())
                .invoiceNumber(request.getInvoiceNumber())
                .invoiceType(request.getInvoiceType())
                .invoiceDate(request.getInvoiceDate())
                .amountBeforeTax(request.getAmountBeforeTax())
                .taxAmount(request.getTaxAmount())
                .totalAmount(request.getTotalAmount())
                .taxRate(request.getTaxRate())
                .supplierId(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .buyerTaxNumber(request.getBuyerTaxNumber())
                .buyerName(request.getBuyerName())
                .goodsDescription(request.getGoodsDescription())
                .drawer(request.getDrawer())
                .payee(request.getPayee())
                .checker(request.getChecker())
                .remark(request.getRemark())
                .status(InvoiceStatusEnum.NORMAL)
                .reimbursementFrozen(false)
                .conclusionDeletable(true)
                .build();

        if (supplier.getIsBlacklisted()) {
            invoice.setStatus(InvoiceStatusEnum.PENDING_REVIEW);
            invoice.setReimbursementFrozen(true);
        }

        Invoice saved = invoiceRepository.save(invoice);
        return toVO(saved);
    }

    @Transactional(readOnly = true)
    public PageResult<InvoiceVO> queryInvoices(InvoiceQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPageNumber(),
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Invoice> page = invoiceRepository.findByConditions(
                request.getInvoiceCode(),
                request.getSupplierName(),
                request.getStatus(),
                request.getStartDate(),
                request.getEndDate(),
                request.getReimbursementFrozen(),
                pageable
        );

        List<InvoiceVO> content = page.getContent().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.<InvoiceVO>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public InvoiceVO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("发票不存在"));
        return toVO(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceEntityById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("发票不存在"));
    }

    @Transactional
    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    private InvoiceVO toVO(Invoice invoice) {
        return InvoiceVO.builder()
                .id(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceType(invoice.getInvoiceType())
                .invoiceDate(invoice.getInvoiceDate())
                .amountBeforeTax(invoice.getAmountBeforeTax())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .taxRate(invoice.getTaxRate())
                .supplierId(invoice.getSupplierId())
                .supplierCode(invoice.getSupplierCode())
                .supplierName(invoice.getSupplierName())
                .buyerTaxNumber(invoice.getBuyerTaxNumber())
                .buyerName(invoice.getBuyerName())
                .goodsDescription(invoice.getGoodsDescription())
                .remark(invoice.getRemark())
                .status(invoice.getStatus())
                .statusDescription(invoice.getStatus().getDescription())
                .reimbursementFrozen(invoice.getReimbursementFrozen())
                .conclusion(invoice.getConclusion())
                .confirmedByName(invoice.getConfirmedByName())
                .confirmedAt(invoice.getConfirmedAt())
                .conclusionDeletable(invoice.getConclusionDeletable())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
