package com.invoice.risk.entity;

import com.invoice.risk.enums.InvoiceStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice", indexes = {
    @Index(name = "idx_invoice_code", columnList = "invoiceCode"),
    @Index(name = "idx_invoice_status", columnList = "status"),
    @Index(name = "idx_supplier_id", columnList = "supplierId"),
    @Index(name = "idx_invoice_date", columnList = "invoiceDate")
})
@EntityListeners(AuditingEntityListener.class)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @Column(nullable = false, length = 20)
    private String invoiceNumber;

    @Column(nullable = false, length = 20)
    private String invoiceType;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amountBeforeTax;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Column(nullable = false)
    private Long supplierId;

    @Column(length = 100)
    private String supplierCode;

    @Column(length = 200)
    private String supplierName;

    @Column(length = 50)
    private String buyerTaxNumber;

    @Column(length = 200)
    private String buyerName;

    @Column(length = 500)
    private String goodsDescription;

    @Column(length = 200)
    private String drawer;

    @Column(length = 200)
    private String payee;

    @Column(length = 200)
    private String checker;

    @Column(length = 500)
    private String remark;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InvoiceStatusEnum status = InvoiceStatusEnum.NORMAL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean reimbursementFrozen = false;

    @Column(length = 500)
    private String conclusion;

    private Long confirmedBy;

    @Column(length = 50)
    private String confirmedByName;

    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean conclusionDeletable = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
