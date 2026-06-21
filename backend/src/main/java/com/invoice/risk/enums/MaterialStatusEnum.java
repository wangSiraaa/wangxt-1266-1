package com.invoice.risk.enums;

public enum MaterialStatusEnum {
    PENDING("PENDING", "待补充"),
    SUPPLEMENTED("SUPPLEMENTED", "已补充");

    private final String code;
    private final String description;

    MaterialStatusEnum(String code, String description) {
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
