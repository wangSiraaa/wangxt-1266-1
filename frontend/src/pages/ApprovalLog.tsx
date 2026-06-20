import React, { useEffect, useState } from 'react'
import {
  Table,
  Input,
  Button,
  Space,
  Tag,
  Pagination,
} from '@arco-design/web-react'
import { IconSearch, IconRefresh, IconFile } from '@arco-design/web-react/icon'
import dayjs from 'dayjs'
import { approvalApi } from '@/api'
import type { ApprovalLogVO, ApprovalActionCode } from '@/types'
import { getApprovalActionLabel } from '@/utils/enumOptions'

const ApprovalLog: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<ApprovalLogVO[]>([])
  const [total, setTotal] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [pageSize, setPageSize] = useState(20)

  const [invoiceCode, setInvoiceCode] = useState('')
  const [operatorName, setOperatorName] = useState('')

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await approvalApi.queryLogs({
        invoiceCode: invoiceCode || undefined,
        operatorName: operatorName || undefined,
        pageNumber,
        pageSize,
      })
      setData(res.content)
      setTotal(res.totalElements)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [pageNumber, pageSize])

  const handleSearch = () => {
    setPageNumber(0)
    setTimeout(loadData, 0)
  }

  const handleReset = () => {
    setInvoiceCode('')
    setOperatorName('')
    setPageNumber(0)
    setTimeout(loadData, 0)
  }

  const getActionColor = (action: ApprovalActionCode): string => {
    const map: Record<string, string> = {
      MARK_RISK: 'orange',
      SUPPLEMENT_MATERIALS: 'blue',
      CONFIRM_RESOLVED: 'green',
      CONFIRM_REJECTED: 'red',
      FREEZE_REIMBURSEMENT: 'red',
      ADD_BLACKLIST: 'red',
      REMOVE_BLACKLIST: 'green',
    }
    return map[action] || 'grey'
  }

  const columns = [
    {
      title: '时间',
      dataIndex: 'createdAt',
      width: 180,
      fixed: 'left' as const,
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作类型',
      dataIndex: 'action',
      width: 200,
      render: (v: ApprovalActionCode) => (
        <Tag color={getActionColor(v)}>{getApprovalActionLabel(v)}</Tag>
      ),
    },
    {
      title: '发票代码',
      dataIndex: 'invoiceCode',
      width: 180,
      render: (v: string) => (
        <Space>
          <IconFile style={{ color: '#165dff' }} />
          <span style={{ fontFamily: 'monospace', color: '#165dff' }}>{v}</span>
        </Space>
      ),
    },
    {
      title: '操作说明',
      dataIndex: 'remark',
      ellipsis: true,
      render: (v: string) => v || '-',
    },
    {
      title: '操作人',
      dataIndex: 'operatorName',
      width: 140,
    },
    {
      title: '操作人角色',
      dataIndex: 'operatorRoleDescription',
      width: 140,
      render: (v: string, r: ApprovalLogVO) => (
        <Tag color="purple">{v || r.operatorRole}</Tag>
      ),
    },
  ]

  return (
    <div>
      <div className="card-wrapper">
        <Space style={{ marginBottom: 16 }}>
          <Input
            style={{ width: 240 }}
            placeholder="搜索发票代码"
            value={invoiceCode}
            onChange={setInvoiceCode}
            allowClear
          />
          <Input
            style={{ width: 200 }}
            placeholder="搜索操作人"
            value={operatorName}
            onChange={setOperatorName}
            allowClear
          />
          <Button type="primary" icon={<IconSearch />} onClick={handleSearch}>
            查询
          </Button>
          <Button icon={<IconRefresh />} onClick={handleReset}>
            重置
          </Button>
        </Space>
      </div>

      <div className="card-wrapper">
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 16,
          }}
        >
          <h3 style={{ margin: 0 }}>审批日志（共 {total} 条）</h3>
        </div>

        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          data={data}
          pagination={false}
          scroll={{ x: 1100 }}
        />

        <div style={{ marginTop: 16, display: 'flex', justifyContent: 'flex-end' }}>
          <Pagination
            total={total}
            current={pageNumber + 1}
            pageSize={pageSize}
            showTotal
            sizeCanChange
            onChange={(page, size) => {
              setPageNumber(page - 1)
              setPageSize(size)
            }}
          />
        </div>
      </div>
    </div>
  )
}

export default ApprovalLog
