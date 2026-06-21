import { httpGet, httpPost } from '@/utils/request'
import type {
  LoginResponse,
  UserInfo,
  InvoiceVO,
  PageResult,
  RiskRecordVO,
  RiskMaterialVO,
  ApprovalLogVO,
  SupplierVO,
  DashboardVO,
  InvoiceStatusCode,
} from '@/types'

export const authApi = {
  login: (username: string, password: string) =>
    httpPost<LoginResponse>('/auth/login', { username, password }),
}

export const dashboardApi = {
  getDashboard: () => httpGet<DashboardVO>('/dashboard'),
}

export const invoiceApi = {
  importInvoice: (data: any) => httpPost<InvoiceVO>('/invoices/import', data),
  queryInvoices: (data: {
    invoiceCode?: string
    supplierName?: string
    status?: InvoiceStatusCode
    startDate?: string
    endDate?: string
    reimbursementFrozen?: boolean
    pageNumber: number
    pageSize: number
  }) => httpPost<PageResult<InvoiceVO>>('/invoices/query', data),
  getInvoiceById: (id: number) => httpGet<InvoiceVO>(`/invoices/${id}`),
}

export const riskApi = {
  markRisk: (data: {
    invoiceId: number
    riskType: string
    riskDescription?: string
    markReason?: string
    requiredMaterials?: string[]
  }) => httpPost<RiskRecordVO>('/risks/mark', data),

  supplementMaterial: (data: {
    invoiceId: number
    materialId?: number
    materialType: string
    materialName?: string
    materialUrl?: string
    contractNumber?: string
    contractDate?: string
    deliveryNoteNumber?: string
    deliveryDate?: string
    remark?: string
  }) => httpPost<RiskMaterialVO>('/risks/materials', data),

  createPendingMaterial: (data: {
    invoiceId: number
    materialType: string
  }) => httpPost<RiskMaterialVO>('/risks/materials/pending', data),

  deletePendingMaterial: (materialId: number) =>
    httpDelete<void>(`/risks/materials/${materialId}`),

  confirmConclusion: (data: {
    invoiceId: number
    resolved: boolean
    conclusion: string
  }) => httpPost<void>('/risks/confirm', data),

  getRiskRecords: (invoiceId: number) =>
    httpGet<RiskRecordVO[]>(`/risks/invoices/${invoiceId}/records`),

  getMaterials: (invoiceId: number) =>
    httpGet<RiskMaterialVO[]>(`/risks/invoices/${invoiceId}/materials`),
}

export const supplierApi = {
  importSupplier: (data: any) => httpPost<SupplierVO>('/suppliers/import', data),
  querySuppliers: (data: {
    keyword?: string
    isBlacklisted?: boolean
    pageNumber: number
    pageSize: number
  }) => httpPost<PageResult<SupplierVO>>('/suppliers/query', data),
  getSupplierById: (id: number) => httpGet<SupplierVO>(`/suppliers/${id}`),
  getAllSuppliers: () => httpGet<SupplierVO[]>('/suppliers/all'),
  addToBlacklist: (supplierId: number, reason: string) =>
    httpPost<void>('/suppliers/blacklist/add', { supplierId, reason }),
  removeFromBlacklist: (supplierId: number, reason: string) =>
    httpPost<void>('/suppliers/blacklist/remove', { supplierId, reason }),
}

export const approvalApi = {
  getLogsByInvoiceId: (invoiceId: number) =>
    httpGet<ApprovalLogVO[]>(`/approval-logs/invoices/${invoiceId}`),
  queryLogs: (params: {
    invoiceCode?: string
    operatorName?: string
    pageNumber: number
    pageSize: number
  }) => httpGet<PageResult<ApprovalLogVO>>('/approval-logs/query', { params }),
}

export const getCurrentUser = (): UserInfo | null => {
  const str = localStorage.getItem('userInfo')
  if (!str) return null
  try {
    return JSON.parse(str) as UserInfo
  } catch {
    return null
  }
}

export const setAuthStorage = (token: string, userInfo: UserInfo) => {
  localStorage.setItem('token', token)
  localStorage.setItem('userInfo', JSON.stringify(userInfo))
}

export const clearAuthStorage = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
}

export const getToken = (): string | null => localStorage.getItem('token')
