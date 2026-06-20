import React, { useEffect, useState } from 'react'
import {
  Table,
  Input,
  Button,
  Select,
  DatePicker,
  Space,
  Tag,
  Modal,
  Form,
  InputNumber,
  Message,
  Popconfirm,
  Switch,
  Grid,
} from '@arco-design/web-react'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconEye,
  IconWarning,
} from '@arco-design/web-react/icon'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { invoiceApi, supplierApi, riskApi } from '@/api'
import { useAuth } from '@/store/AuthContext'
import { INVOICE_STATUS_OPTIONS, getInvoiceStatusColor, getInvoiceStatusLabel } from '@/utils/enumOptions'
import type { InvoiceVO, SupplierVO, InvoiceStatusCode, RiskTypeCode } from '@/types'
import { RISK_TYPE_OPTIONS } from '@/utils/enumOptions'

const FormItem = Form.Item
const Option = Select.Option
const RangePicker = DatePicker.RangePicker
const Row = Grid.Row
const Col = Grid.Col

const InvoiceList: React.FC = () => {
  const navigate = useNavigate()
  const { hasRole } = useAuth()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<InvoiceVO[]>([])
  const [total, setTotal] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [pageSize, setPageSize] = useState(10)

  const [searchParams, setSearchParams] = useState({
    invoiceCode: '',
    supplierName: '',
    status: undefined as InvoiceStatusCode | undefined,
    startDate: '' as string,
    endDate: '' as string,
    reimbursementFrozen: undefined as boolean | undefined,
  })

  const [importVisible, setImportVisible] = useState(false)
  const [markRiskVisible, setMarkRiskVisible] = useState(false)
  const [currentInvoice, setCurrentInvoice] = useState<InvoiceVO | null>(null)
  const [importForm] = Form.useForm()
  const [markRiskForm] = Form.useForm()
  const [suppliers, setSuppliers] = useState<SupplierVO[]>([])
  const [submitting, setSubmitting] = useState(false)

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await invoiceApi.queryInvoices({
        ...searchParams,
        pageNumber,
        pageSize,
      })
      setData(res.content)
      setTotal(res.totalElements)
    } finally {
      setLoading(false)
    }
  }

  const loadSuppliers = async () => {
    try {
      const res = await supplierApi.getAllSuppliers()
      setSuppliers(res)
    } catch (err) {
      // ignore
    }
  }

  useEffect(() => {
    loadData()
    loadSuppliers()
  }, [pageNumber, pageSize])

  const handleSearch = () => {
    setPageNumber(0)
    setTimeout(loadData, 0)
  }

  const handleReset = () => {
    setSearchParams({
      invoiceCode: '',
      supplierName: '',
      status: undefined,
      startDate: '',
      endDate: '',
      reimbursementFrozen: undefined,
    })
    setPageNumber(0)
    setTimeout(loadData, 0)
  }

  const handleImportInvoice = async (values: any) => {
    setSubmitting(true)
    try {
      const supplier = suppliers.find((s) => s.id === values.supplierId)
      await invoiceApi.importInvoice({
        ...values,
        invoiceDate: values.invoiceDate
          ? dayjs(values.invoiceDate).format('YYYY-MM-DD')
          : undefined,
        supplierCode: supplier?.supplierCode,
        supplierName: supplier?.supplierName,
      })
      Message.success('发票导入成功')
      setImportVisible(false)
      importForm.resetFields()
      loadData()
    } finally {
      setSubmitting(false)
    }
  }

  const handleMarkRisk = async (values: any) => {
    if (!currentInvoice) return
    setSubmitting(true)
    try {
      await riskApi.markRisk({
        invoiceId: currentInvoice.id,
        riskType: values.riskType,
        riskDescription: values.riskDescription,
        markReason: values.markReason,
      })
      Message.success('风险标记成功')
      setMarkRiskVisible(false)
      markRiskForm.resetFields()
      setCurrentInvoice(null)
      loadData()
    } finally {
      setSubmitting(false)
    }
  }

  const columns = [
    {
      title: '发票代码',
      dataIndex: 'invoiceCode',
      width: 160,
      render: (v: string, record: InvoiceVO) => (
        <a onClick={() => navigate(`/invoices/${record.id}`)} style={{ color: '#165dff' }}>
          {v}
        </a>
      ),
    },
    {
      title: '发票号码',
      dataIndex: 'invoiceNumber',
      width: 120,
    },
    {
      title: '发票类型',
      dataIndex: 'invoiceType',
      width: 120,
    },
    {
      title: '开票日期',
      dataIndex: 'invoiceDate',
      width: 120,
    },
    {
      title: '价税合计',
      dataIndex: 'totalAmount',
      width: 120,
      align: 'right' as const,
      render: (v: number) => `¥${Number(v).toFixed(2)}`,
    },
    {
      title: '供应商',
      dataIndex: 'supplierName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 140,
      render: (v: InvoiceStatusCode, record: InvoiceVO) => (
        <Space direction="vertical" size={2}>
          <Tag color={getInvoiceStatusColor(v)}>{getInvoiceStatusLabel(v)}</Tag>
          {record.reimbursementFrozen && <Tag color="red">报销已冻结</Tag>}
          {!record.conclusionDeletable && (
            <Tag color="arcoblue">不可删除结论</Tag>
          )}
        </Space>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      width: 170,
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right' as const,
      render: (_: any, record: InvoiceVO) => (
        <Space size="small">
          <Button
            type="text"
            size="small"
            icon={<IconEye />}
            onClick={() => navigate(`/invoices/${record.id}`)}
          >
            详情
          </Button>
          {hasRole(['TAX_SPECIALIST']) &&
            (record.status === 'NORMAL' ||
              record.status === 'PENDING_REVIEW' ||
              record.conclusionDeletable) && (
              <Button
                type="text"
                size="small"
                status="warning"
                icon={<IconWarning />}
                onClick={() => {
                  setCurrentInvoice(record)
                  setMarkRiskVisible(true)
                }}
              >
                标记风险
              </Button>
            )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="card-wrapper">
        <Row gutter={16} style={{ marginBottom: 12 }}>
          <Col span={6}>
            <FormItem label="发票代码" field="invoiceCode" style={{ marginBottom: 0 }}>
              <Input
                placeholder="请输入"
                value={searchParams.invoiceCode}
                onChange={(v) => setSearchParams({ ...searchParams, invoiceCode: v })}
                allowClear
              />
            </FormItem>
          </Col>
          <Col span={6}>
            <FormItem label="供应商名称" style={{ marginBottom: 0 }}>
              <Input
                placeholder="请输入"
                value={searchParams.supplierName}
                onChange={(v) => setSearchParams({ ...searchParams, supplierName: v })}
                allowClear
              />
            </FormItem>
          </Col>
          <Col span={4}>
            <FormItem label="状态" style={{ marginBottom: 0 }}>
              <Select
                placeholder="全部"
                style={{ width: '100%' }}
                value={searchParams.status}
                onChange={(v) =>
                  setSearchParams({ ...searchParams, status: v as InvoiceStatusCode })
                }
                allowClear
              >
                {INVOICE_STATUS_OPTIONS.map((opt) => (
                  <Option key={opt.value} value={opt.value}>
                    {opt.label}
                  </Option>
                ))}
              </Select>
            </FormItem>
          </Col>
          <Col span={4}>
            <FormItem label="报销冻结" style={{ marginBottom: 0 }}>
              <Select
                placeholder="全部"
                style={{ width: '100%' }}
                value={searchParams.reimbursementFrozen}
                onChange={(v) => setSearchParams({ ...searchParams, reimbursementFrozen: v })}
                allowClear
              >
                <Option value={true}>已冻结</Option>
                <Option value={false}>未冻结</Option>
              </Select>
            </FormItem>
          </Col>
        </Row>
        <Row gutter={16}>
          <Col span={10}>
            <FormItem label="开票日期" style={{ marginBottom: 0 }}>
              <RangePicker
                style={{ width: '100%' }}
                value={
                  searchParams.startDate && searchParams.endDate
                    ? [dayjs(searchParams.startDate), dayjs(searchParams.endDate)]
                    : []
                }
                onChange={(values: any) => {
                  setSearchParams({
                    ...searchParams,
                    startDate: values?.[0] ? dayjs(values[0]).format('YYYY-MM-DD') : '',
                    endDate: values?.[1] ? dayjs(values[1]).format('YYYY-MM-DD') : '',
                  })
                }}
              />
            </FormItem>
          </Col>
          <Col span={14}>
            <Space>
              <Button type="primary" icon={<IconSearch />} onClick={handleSearch}>
                查询
              </Button>
              <Button icon={<IconRefresh />} onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Col>
        </Row>
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
          <h3 style={{ margin: 0 }}>发票列表（共 {total} 条）</h3>
          <Space>
            <Button type="primary" icon={<IconPlus />} onClick={() => setImportVisible(true)}>
              导入发票
            </Button>
          </Space>
        </div>

        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          data={data}
          pagination={{
            total,
            current: pageNumber + 1,
            pageSize,
            showTotal: true,
            sizeCanChange: true,
            onChange: (page, size) => {
              setPageNumber(page - 1)
              setPageSize(size)
            },
          }}
          scroll={{ x: 1400 }}
        />
      </div>

      <Modal
        title="导入发票"
        visible={importVisible}
        onOk={() => importForm.submit()}
        onCancel={() => {
          setImportVisible(false)
          importForm.resetFields()
        }}
        confirmLoading={submitting}
        style={{ width: 600 }}
      >
        <Form
          form={importForm}
          layout="vertical"
          onSubmit={handleImportInvoice}
          requiredSymbol={false}
        >
          <Row gutter={12}>
            <Col span={12}>
              <FormItem
                label="发票代码"
                field="invoiceCode"
                rules={[{ required: true, message: '请输入发票代码' }]}
              >
                <Input placeholder="请输入" />
              </FormItem>
            </Col>
            <Col span={12}>
              <FormItem
                label="发票号码"
                field="invoiceNumber"
                rules={[{ required: true, message: '请输入发票号码' }]}
              >
                <Input placeholder="请输入" />
              </FormItem>
            </Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}>
              <FormItem
                label="发票类型"
                field="invoiceType"
                rules={[{ required: true, message: '请选择' }]}
              >
                <Select placeholder="请选择">
                  <Option value="增值税专用发票">增值税专用发票</Option>
                  <Option value="增值税普通发票">增值税普通发票</Option>
                  <Option value="电子普通发票">电子普通发票</Option>
                  <Option value="电子专用发票">电子专用发票</Option>
                  <Option value="其他">其他</Option>
                </Select>
              </FormItem>
            </Col>
            <Col span={12}>
              <FormItem
                label="开票日期"
                field="invoiceDate"
                rules={[{ required: true, message: '请选择' }]}
              >
                <DatePicker style={{ width: '100%' }} />
              </FormItem>
            </Col>
          </Row>
          <FormItem
            label="供应商"
            field="supplierId"
            rules={[{ required: true, message: '请选择供应商' }]}
          >
            <Select placeholder="请选择供应商">
              {suppliers.map((s) => (
                <Option key={s.id} value={s.id}>
                  {s.supplierCode} - {s.supplierName}
                  {s.isBlacklisted && ' （黑名单）'}
                </Option>
              ))}
            </Select>
          </FormItem>
          <Row gutter={12}>
            <Col span={8}>
              <FormItem
                label="税前金额"
                field="amountBeforeTax"
                rules={[{ required: true, message: '请输入' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  precision={2}
                  min={0}
                  placeholder="0.00"
                />
              </FormItem>
            </Col>
            <Col span={8}>
              <FormItem
                label="税额"
                field="taxAmount"
                rules={[{ required: true, message: '请输入' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  precision={2}
                  min={0}
                  placeholder="0.00"
                />
              </FormItem>
            </Col>
            <Col span={8}>
              <FormItem
                label="价税合计"
                field="totalAmount"
                rules={[{ required: true, message: '请输入' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  precision={2}
                  min={0}
                  placeholder="0.00"
                />
              </FormItem>
            </Col>
          </Row>
          <FormItem label="货物或服务名称" field="goodsDescription">
            <Input.TextArea placeholder="请输入" rows={2} />
          </FormItem>
          <FormItem label="备注" field="remark">
            <Input.TextArea placeholder="请输入" rows={2} />
          </FormItem>
        </Form>
      </Modal>

      <Modal
        title="标记风险"
        visible={markRiskVisible}
        onOk={() => markRiskForm.submit()}
        onCancel={() => {
          setMarkRiskVisible(false)
          markRiskForm.resetFields()
          setCurrentInvoice(null)
        }}
        confirmLoading={submitting}
      >
        {currentInvoice && (
          <div style={{ marginBottom: 16, padding: 12, background: '#f7f8fa', borderRadius: 4 }}>
            <div>发票代码：{currentInvoice.invoiceCode}</div>
            <div>供应商：{currentInvoice.supplierName}</div>
            <div>
              价税合计：¥{Number(currentInvoice.totalAmount).toFixed(2)}
            </div>
          </div>
        )}
        <Form
          form={markRiskForm}
          layout="vertical"
          onSubmit={handleMarkRisk}
          requiredSymbol={false}
        >
          <FormItem
            label="风险类型"
            field="riskType"
            rules={[{ required: true, message: '请选择风险类型' }]}
          >
            <Select placeholder="请选择">
              {RISK_TYPE_OPTIONS.map((opt) => (
                <Option key={opt.value} value={opt.value}>
                  {opt.label}
                </Option>
              ))}
            </Select>
          </FormItem>
          <FormItem label="风险描述" field="riskDescription">
            <Input.TextArea placeholder="请输入风险详细描述" rows={3} />
          </FormItem>
          <FormItem label="标记原因" field="markReason">
            <Input.TextArea placeholder="请输入标记原因" rows={3} />
          </FormItem>
        </Form>
      </Modal>
    </div>
  )
}

export default InvoiceList
