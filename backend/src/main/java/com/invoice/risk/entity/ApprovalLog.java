package com.invoice.risk.entity;

import com.invoice.risk.enums.ApprovalActionEnum;
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
@Table(name = "approval_log")
@EntityListeners(AuditingEntityListener.class)
public class ApprovalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(length = 50)
    private String invoiceCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalActionEnum action;

    @Column(length = 1000)
    private String remark;

    @Column(nullable = false)
    private Long operatorId;

    @Column(length = 50)
    private String operatorName;

    @Column(length = 50)
    private String operatorRole;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
