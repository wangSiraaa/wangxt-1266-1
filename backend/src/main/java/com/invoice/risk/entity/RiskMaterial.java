package com.invoice.risk.entity;

import com.invoice.risk.enums.MaterialStatusEnum;
import com.invoice.risk.enums.MaterialTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "risk_material")
@EntityListeners(AuditingEntityListener.class)
public class RiskMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(length = 50)
    private String invoiceCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaterialTypeEnum materialType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MaterialStatusEnum materialStatus = MaterialStatusEnum.PENDING;

    @Column(length = 200)
    private String materialName;

    @Column(length = 500)
    private String materialUrl;

    @Column(length = 50)
    private String contractNumber;

    private java.time.LocalDate contractDate;

    @Column(length = 50)
    private String deliveryNoteNumber;

    private java.time.LocalDate deliveryDate;

    @Column(length = 500)
    private String remark;

    private Long uploadedBy;

    @Column(length = 50)
    private String uploadedByName;

    private LocalDateTime uploadedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
