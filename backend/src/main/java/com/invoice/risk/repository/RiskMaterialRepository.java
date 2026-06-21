package com.invoice.risk.repository;

import com.invoice.risk.entity.RiskMaterial;
import com.invoice.risk.enums.MaterialStatusEnum;
import com.invoice.risk.enums.MaterialTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskMaterialRepository extends JpaRepository<RiskMaterial, Long> {
    List<RiskMaterial> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);
    List<RiskMaterial> findByInvoiceIdAndMaterialType(Long invoiceId, MaterialTypeEnum materialType);
    boolean existsByInvoiceIdAndMaterialType(Long invoiceId, MaterialTypeEnum materialType);
    List<RiskMaterial> findByInvoiceIdAndMaterialStatus(Long invoiceId, MaterialStatusEnum status);
    boolean existsByInvoiceIdAndMaterialTypeAndMaterialStatus(Long invoiceId, MaterialTypeEnum materialType, MaterialStatusEnum status);
}
