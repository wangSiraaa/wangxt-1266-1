package com.invoice.risk.enums;

public enum InvoiceStatusEnum {
    NORMAL("NORMAL", "正常"),
    PENDING_REVIEW("PENDING_REVIEW", "待审核"),
    RISK_IDENTIFIED("RISK_IDENTIFIED", "已标记风险"),
    MATERIALS_SUPPLEMENTED("MATERIALS_SUPPLEMENTED", "材料已补充"),
    PENDING_CONFIRM("PENDING_CONFIRM", "待经理确认"),
    RESOLVED("RESOLVED", "风险解除"),
    REJECTED("REJECTED", "风险确认（异常）"),
    FROZEN("FROZEN", "已冻结报销");

    private final String code;
    private final String description;

    InvoiceStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
