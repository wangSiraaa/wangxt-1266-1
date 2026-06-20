package com.invoice.risk.enums;

public enum ApprovalActionEnum {
    MARK_RISK("MARK_RISK", "税务专员标记风险"),
    SUPPLEMENT_MATERIALS("SUPPLEMENT_MATERIALS", "采购负责人补充材料"),
    CONFIRM_RESOLVED("CONFIRM_RESOLVED", "经理确认解除风险"),
    CONFIRM_REJECTED("CONFIRM_REJECTED", "经理确认风险异常"),
    FREEZE_REIMBURSEMENT("FREEZE_REIMBURSEMENT", "冻结报销"),
    ADD_BLACKLIST("ADD_BLACKLIST", "加入黑名单"),
    REMOVE_BLACKLIST("REMOVE_BLACKLIST", "移出黑名单");

    private final String code;
    private final String description;

    ApprovalActionEnum(String code, String description) {
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
