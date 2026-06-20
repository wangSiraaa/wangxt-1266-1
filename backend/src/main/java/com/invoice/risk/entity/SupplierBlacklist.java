package com.invoice.risk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "supplier_blacklist")
@EntityListeners(AuditingEntityListener.class)
public class SupplierBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Column(length = 100)
    private String supplierCode;

    @Column(length = 200)
    private String supplierName;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private Long operatorId;

    @Column(length = 50)
    private String operatorName;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime removedAt;

    private Long removedBy;

    @Column(length = 500)
    private String removeReason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
