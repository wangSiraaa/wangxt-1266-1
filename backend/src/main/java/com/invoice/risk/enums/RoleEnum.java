package com.invoice.risk.enums;

public enum RoleEnum {
    TAX_SPECIALIST("TAX_SPECIALIST", "税务专员"),
    PROCUREMENT_HEAD("PROCUREMENT_HEAD", "采购负责人"),
    FINANCE_MANAGER("FINANCE_MANAGER", "财务经理");

    private final String code;
    private final String description;

    RoleEnum(String code, String description) {
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
