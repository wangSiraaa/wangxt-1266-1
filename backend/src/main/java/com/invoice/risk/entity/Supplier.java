package com.invoice.risk.entity;

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
@Table(name = "supplier")
@EntityListeners(AuditingEntityListener.class)
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String supplierCode;

    @Column(nullable = false, length = 200)
    private String supplierName;

    @Column(nullable = false, length = 50)
    private String taxNumber;

    @Column(length = 200)
    private String address;

    @Column(length = 30)
    private String contactPhone;

    @Column(length = 100)
    private String bankName;

    @Column(length = 50)
    private String bankAccount;

    @Column(precision = 18, scale = 2)
    private BigDecimal creditLimit;

    @Column(nullable = false)
    private Boolean isBlacklisted;

    @Column(length = 500)
    private String remark;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
