export interface Result<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  pageNumber: number
  pageSize: number
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: RoleCode
  roleDescription: string
  phone?: string
  email?: string
}

export type RoleCode = 'TAX_SPECIALIST' | 'PROCUREMENT_HEAD' | 'FINANCE_MANAGER'

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export type InvoiceStatusCode =
  | 'NORMAL'
  | 'PENDING_REVIEW'
  | 'RISK_IDENTIFIED'
  | 'MATERIALS_SUPPLEMENTED'
  | 'PENDING_CONFIRM'
  | 'RESOLVED'
  | 'REJECTED'
  | 'FROZEN'

export interface InvoiceVO {
  id: number
  invoiceCode: string
  invoiceNumber: string
  invoiceType: string
  invoiceDate: string
  amountBeforeTax: number
  taxAmount: number
  totalAmount: number
  taxRate?: number
  supplierId: number
  supplierCode: string
  supplierName: string
  buyerTaxNumber?: string
  buyerName?: string
  goodsDescription?: string
  remark?: string
  status: InvoiceStatusCode
  statusDescription: string
  reimbursementFrozen: boolean
  conclusion?: string
  confirmedByName?: string
  confirmedAt?: string
  conclusionDeletable: boolean
  createdAt: string
  updatedAt: string
}

export type RiskTypeCode =
  | 'INVOICE_FORGERY'
  | 'TAX_NUMBER_MISMATCH'
  | 'AMOUNT_MISMATCH'
  | 'REPEATED_INVOICE'
  | 'OVERDUE_INVOICE'
  | 'SUPPLIER_BLACKLIST'
  | 'GOODS_MISMATCH'
  | 'NO_CONTRACT'
  | 'OTHER'

export interface RiskRecordVO {
  id: number
  invoiceId: number
  invoiceCode: string
  riskType: RiskTypeCode
  riskTypeDescription: string
  riskDescription?: string
  markReason?: string
  markedBy: number
  markedByName: string
  isResolved: boolean
  resolveDescription?: string
  resolvedByName?: string
  resolvedAt?: string
  createdAt: string
  updatedAt: string
}

export type MaterialTypeCode =
  | 'CONTRACT'
  | 'DELIVERY_NOTE'
  | 'PURCHASE_ORDER'
  | 'WAREHOUSE_RECEIPT'
  | 'OTHER'

export type MaterialStatusCode = 'PENDING' | 'SUPPLEMENTED'

export interface RiskMaterialVO {
  id: number
  invoiceId: number
  invoiceCode: string
  materialType: MaterialTypeCode
  materialTypeDescription: string
  materialStatus: MaterialStatusCode
  materialStatusDescription: string
  materialName?: string
  materialUrl?: string
  contractNumber?: string
  contractDate?: string
  deliveryNoteNumber?: string
  deliveryDate?: string
  remark?: string
  uploadedByName?: string
  uploadedAt?: string
  createdAt: string
  updatedAt: string
}

export type ApprovalActionCode =
  | 'MARK_RISK'
  | 'SUPPLEMENT_MATERIALS'
  | 'CONFIRM_RESOLVED'
  | 'CONFIRM_REJECTED'
  | 'FREEZE_REIMBURSEMENT'
  | 'ADD_BLACKLIST'
  | 'REMOVE_BLACKLIST'

export interface ApprovalLogVO {
  id: number
  invoiceId: number
  invoiceCode: string
  action: ApprovalActionCode
  actionDescription: string
  remark?: string
  operatorId: number
  operatorName: string
  operatorRole: string
  operatorRoleDescription: string
  createdAt: string
}

export interface SupplierVO {
  id: number
  supplierCode: string
  supplierName: string
  taxNumber: string
  address?: string
  contactPhone?: string
  bankName?: string
  bankAccount?: string
  creditLimit?: number
  isBlacklisted: boolean
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface DashboardVO {
  totalInvoices: number
  normalInvoices: number
  pendingReviewInvoices: number
  riskIdentifiedInvoices: number
  materialsSupplementedInvoices: number
  pendingConfirmInvoices: number
  resolvedInvoices: number
  rejectedInvoices: number
  frozenInvoices: number
  totalSuppliers: number
  blacklistedSuppliers: number
  pendingRisks: number
}
