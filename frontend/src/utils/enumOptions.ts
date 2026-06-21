import type { InvoiceStatusCode, RiskTypeCode, MaterialTypeCode, MaterialStatusCode, RoleCode, ApprovalActionCode } from '@/types'

export const INVOICE_STATUS_OPTIONS: { label: string; value: InvoiceStatusCode; color?: string }[] = [
  { label: '正常', value: 'NORMAL', color: 'green' },
  { label: '待审核', value: 'PENDING_REVIEW', color: 'cyan' },
  { label: '已标记风险', value: 'RISK_IDENTIFIED', color: 'orange' },
  { label: '材料已补充', value: 'MATERIALS_SUPPLEMENTED', color: 'blue' },
  { label: '待经理确认', value: 'PENDING_CONFIRM', color: 'purple' },
  { label: '风险解除', value: 'RESOLVED', color: 'green' },
  { label: '风险确认（异常）', value: 'REJECTED', color: 'red' },
  { label: '已冻结报销', value: 'FROZEN', color: 'red' },
]

export const RISK_TYPE_OPTIONS: { label: string; value: RiskTypeCode }[] = [
  { label: '发票伪造', value: 'INVOICE_FORGERY' },
  { label: '税号不符', value: 'TAX_NUMBER_MISMATCH' },
  { label: '金额异常', value: 'AMOUNT_MISMATCH' },
  { label: '重复发票', value: 'REPEATED_INVOICE' },
  { label: '发票逾期', value: 'OVERDUE_INVOICE' },
  { label: '供应商黑名单命中', value: 'SUPPLIER_BLACKLIST' },
  { label: '货物与合同不符', value: 'GOODS_MISMATCH' },
  { label: '无对应合同', value: 'NO_CONTRACT' },
  { label: '其他风险', value: 'OTHER' },
]

export const MATERIAL_TYPE_OPTIONS: { label: string; value: MaterialTypeCode }[] = [
  { label: '采购合同', value: 'CONTRACT' },
  { label: '收货单', value: 'DELIVERY_NOTE' },
  { label: '采购订单', value: 'PURCHASE_ORDER' },
  { label: '入库单', value: 'WAREHOUSE_RECEIPT' },
  { label: '其他材料', value: 'OTHER' },
]

export const ROLE_OPTIONS: { label: string; value: RoleCode }[] = [
  { label: '税务专员', value: 'TAX_SPECIALIST' },
  { label: '采购负责人', value: 'PROCUREMENT_HEAD' },
  { label: '财务经理', value: 'FINANCE_MANAGER' },
]

export const APPROVAL_ACTION_OPTIONS: { label: string; value: ApprovalActionCode }[] = [
  { label: '税务专员标记风险', value: 'MARK_RISK' },
  { label: '采购负责人补充材料', value: 'SUPPLEMENT_MATERIALS' },
  { label: '经理确认解除风险', value: 'CONFIRM_RESOLVED' },
  { label: '经理确认风险异常', value: 'CONFIRM_REJECTED' },
  { label: '冻结报销', value: 'FREEZE_REIMBURSEMENT' },
  { label: '加入黑名单', value: 'ADD_BLACKLIST' },
  { label: '移出黑名单', value: 'REMOVE_BLACKLIST' },
]

export const INVOICE_TYPE_OPTIONS = [
  { label: '增值税专用发票', value: '增值税专用发票' },
  { label: '增值税普通发票', value: '增值税普通发票' },
  { label: '电子普通发票', value: '电子普通发票' },
  { label: '电子专用发票', value: '电子专用发票' },
  { label: '其他', value: '其他' },
]

export const getInvoiceStatusLabel = (code: InvoiceStatusCode) =>
  INVOICE_STATUS_OPTIONS.find((i) => i.value === code)?.label || code

export const getInvoiceStatusColor = (code: InvoiceStatusCode) =>
  INVOICE_STATUS_OPTIONS.find((i) => i.value === code)?.color || 'grey'

export const getRiskTypeLabel = (code: RiskTypeCode) =>
  RISK_TYPE_OPTIONS.find((i) => i.value === code)?.label || code

export const MATERIAL_STATUS_OPTIONS: { label: string; value: MaterialStatusCode; color?: string }[] = [
  { label: '待补充', value: 'PENDING', color: 'orange' },
  { label: '已补充', value: 'SUPPLEMENTED', color: 'green' },
]

export const getMaterialTypeLabel = (code: MaterialTypeCode) =>
  MATERIAL_TYPE_OPTIONS.find((i) => i.value === code)?.label || code

export const getMaterialStatusLabel = (code: MaterialStatusCode) =>
  MATERIAL_STATUS_OPTIONS.find((i) => i.value === code)?.label || code

export const getMaterialStatusColor = (code: MaterialStatusCode) =>
  MATERIAL_STATUS_OPTIONS.find((i) => i.value === code)?.color || 'grey'

export const getRoleLabel = (code: RoleCode) =>
  ROLE_OPTIONS.find((i) => i.value === code)?.label || code

export const getApprovalActionLabel = (code: ApprovalActionCode) =>
  APPROVAL_ACTION_OPTIONS.find((i) => i.value === code)?.label || code
