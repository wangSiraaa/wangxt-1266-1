package com.invoice.risk.enums;

public enum RiskTypeEnum {
    INVOICE_FORGERY("INVOICE_FORGERY", "发票伪造"),
    TAX_NUMBER_MISMATCH("TAX_NUMBER_MISMATCH", "税号不符"),
    AMOUNT_MISMATCH("AMOUNT_MISMATCH", "金额异常"),
    REPEATED_INVOICE("REPEATED_INVOICE", "重复发票"),
    OVERDUE_INVOICE("OVERDUE_INVOICE", "发票逾期"),
    SUPPLIER_BLACKLIST("SUPPLIER_BLACKLIST", "供应商黑名单命中"),
    GOODS_MISMATCH("GOODS_MISMATCH", "货物与合同不符"),
    NO_CONTRACT("NO_CONTRACT", "无对应合同"),
    OTHER("OTHER", "其他风险");

    private final String code;
    private final String description;

    RiskTypeEnum(String code, String description) {
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
