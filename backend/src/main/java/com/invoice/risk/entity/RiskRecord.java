package com.invoice.risk.entity;

import com.invoice.risk.enums.RiskTypeEnum;
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
@Table(name = "risk_record")
@EntityListeners(AuditingEntityListener.class)
public class RiskRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(length = 50)
    private String invoiceCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RiskTypeEnum riskType;

    @Column(length = 1000)
    private String riskDescription;

    @Column(length = 500)
    private String markReason;

    @Column(nullable = false)
    private Long markedBy;

    @Column(length = 50)
    private String markedByName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isResolved = false;

    @Column(length = 1000)
    private String resolveDescription;

    private Long resolvedBy;

    @Column(length = 50)
    private String resolvedByName;

    private LocalDateTime resolvedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
