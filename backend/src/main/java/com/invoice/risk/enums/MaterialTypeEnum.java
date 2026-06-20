package com.invoice.risk.enums;

public enum MaterialTypeEnum {
    CONTRACT("CONTRACT", "采购合同"),
    DELIVERY_NOTE("DELIVERY_NOTE", "收货单"),
    PURCHASE_ORDER("PURCHASE_ORDER", "采购订单"),
    WAREHOUSE_RECEIPT("WAREHOUSE_RECEIPT", "入库单"),
    OTHER("OTHER", "其他材料");

    private final String code;
    private final String description;

    MaterialTypeEnum(String code, String description) {
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
