import React, { useEffect, useState } from 'react'
import {
  Table,
  Input,
  Button,
  Select,
  Space,
  Tag,
  Modal,
  Form,
  InputNumber,
  Message,
  Popconfirm,
  Drawer,
  Descriptions,
  Grid,
} from '@arco-design/web-react'
import {
  IconPlus,
  IconSearch,
  IconRefresh,
  IconEye,
  IconForbidFill,
  IconUnlockFill,
  IconFile,
} from '@arco-design/web-react/icon'
import dayjs from 'dayjs'
import { supplierApi, invoiceApi } from '@/api'
import { useAuth } from '@/store/AuthContext'
import type { SupplierVO, InvoiceVO } from '@/types'

const FormItem = Form.Item
const Option = Select.Option
const Row = Grid.Row
const Col = Grid.Col

const SupplierList: React.FC = () => {
  const { hasRole } = useAuth()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<SupplierVO[]>([])
  const [total, setTotal] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [pageSize, setPageSize] = useState(10)

  const [searchParams, setSearchParams] = useState({
    keyword: '',
    isBlacklisted: undefined as boolean | undefined,
  })

  const [importVisible, setImportVisible] = useState(false)
  const [importForm] = Form.useForm()
  const [submitting, setSubmitting] = useState(false)

  const [detailVisible, setDetailVisible] = useState(false)
  const [currentSupplier, setCurrentSupplier] = useState<SupplierVO | null>(null)
  const [invoices, setInvoices] = useState<InvoiceVO[]>([])

  const [blacklistReasonVisible, setBlacklistReasonVisible] = useState<
    'add' | 'remove' | null
  >(null)
  const [blacklistForm] = Form.useForm()

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await supplierApi.querySuppliers({
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

  useEffect(() => {
    loadData()
  }, [pageNumber, pageSize])

  const handleSearch = () => {
    setPageNumber(0)
    setTimeout(loadData, 0)
  }

  const handleReset = () => {
    setSearchParams({ keyword: '', isBlacklisted: undefined })
    setPageNumber(0)
    setTimeout(loadData, 0)
  }

  const handleImport = async (values: any) => {
    setSubmitting(true)
    try {
      await supplierApi.importSupplier(values)
      Message.success('供应商导入成功')
      setImportVisible(false)
      importForm.resetFields()
      loadData()
    } finally {
      setSubmitting(false)
    }
  }

  const handleViewDetail = async (supplier: SupplierVO) => {
    setCurrentSupplier(supplier)
    setDetailVisible(true)
    try {
      const res = await invoiceApi.queryInvoices({
        supplierName: supplier.supplierName,
        pageNumber: 0,
        pageSize: 50,
      })
      setInvoices(res.content)
    } catch (err) {
      setInvoices([])
    }
  }

  const handleAddBlacklist = () => {
    blacklistForm.submit()
  }

  const handleRemoveBlacklist = () => {
    blacklistForm.submit()
  }

  const submitBlacklist = async (values: any) => {
    if (!currentSupplier) return
    setSubmitting(true)
    try {
      if (blacklistReasonVisible === 'add') {
        await supplierApi.addToBlacklist(currentSupplier.id, values.reason)
        Message.success('已加入黑名单，关联发票报销已冻结')
      } else {
        await supplierApi.removeFromBlacklist(currentSupplier.id, values.reason)
        Message.success('已移出黑名单，关联发票报销已解冻')
      }
      setBlacklistReasonVisible(null)
      blacklistForm.resetFields()
      setDetailVisible(false)
      setCurrentSupplier(null)
      loadData()
    } finally {
      setSubmitting(false)
    }
  }

  const invoiceColumns = [
    {
      title: '发票代码',
      dataIndex: 'invoiceCode',
      width: 160,
    },
    { title: '发票号码', dataIndex: 'invoiceNumber', width: 100 },
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
      title: '状态',
      dataIndex: 'status',
      width: 120,
      render: (v: string, r: InvoiceVO) => (
        <Space direction="vertical" size={2}>
          <Tag>{r.statusDescription || v}</Tag>
          {r.reimbursementFrozen && <Tag color="red">报销冻结</Tag>}
        </Space>
      ),
    },
  ]

  const columns = [
    {
      title: '供应商编码',
      dataIndex: 'supplierCode',
      width: 140,
    },
    {
      title: '供应商名称',
      dataIndex: 'supplierName',
      width: 220,
      ellipsis: true,
    },
    {
      title: '税号',
      dataIndex: 'taxNumber',
      width: 180,
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      width: 140,
      render: (v: string) => v || '-',
    },
    {
      title: '信用额度',
      dataIndex: 'creditLimit',
      width: 140,
      align: 'right' as const,
      render: (v: number) =>
        v !== undefined && v !== null ? `¥${Number(v).toFixed(2)}` : '-',
    },
    {
      title: '黑名单状态',
      dataIndex: 'isBlacklisted',
      width: 120,
      render: (v: boolean) =>
        v ? (
          <Tag color="red" icon={<IconForbidFill />}>
            黑名单
          </Tag>
        ) : (
          <Tag color="green">正常</Tag>
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
      width: 200,
      fixed: 'right' as const,
      render: (_: any, record: SupplierVO) => (
        <Space size="small">
          <Button
            type="text"
            size="small"
            icon={<IconEye />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          {!record.isBlacklisted ? (
            <Button
              type="text"
              size="small"
              status="danger"
              icon={<IconForbidFill />}
              onClick={() => {
                setCurrentSupplier(record)
                setBlacklistReasonVisible('add')
              }}
            >
              加入黑名单
            </Button>
          ) : (
            <Button
              type="text"
              size="small"
              status="success"
              icon={<IconUnlockFill />}
              onClick={() => {
                setCurrentSupplier(record)
                setBlacklistReasonVisible('remove')
              }}
            >
              移出黑名单
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="card-wrapper">
        <Row gutter={16} style={{ alignItems: 'center' }}>
          <Col span={8}>
            <FormItem label="关键词（编码/名称）" style={{ marginBottom: 0 }}>
              <Input
                placeholder="请输入"
                value={searchParams.keyword}
                onChange={(v) => setSearchParams({ ...searchParams, keyword: v })}
                allowClear
              />
            </FormItem>
          </Col>
          <Col span={4}>
            <FormItem label="黑名单状态" style={{ marginBottom: 0 }}>
              <Select
                placeholder="全部"
                style={{ width: '100%' }}
                value={searchParams.isBlacklisted}
                onChange={(v) =>
                  setSearchParams({ ...searchParams, isBlacklisted: v as boolean })
                }
                allowClear
              >
                <Option value={true}>黑名单</Option>
                <Option value={false}>正常</Option>
              </Select>
            </FormItem>
          </Col>
          <Col span={12}>
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
          <h3 style={{ margin: 0 }}>供应商列表（共 {total} 条）</h3>
          <Button type="primary" icon={<IconPlus />} onClick={() => setImportVisible(true)}>
            导入供应商
          </Button>
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
          scroll={{ x: 1300 }}
        />
      </div>

      <Modal
        title="导入供应商"
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
          onSubmit={handleImport}
          requiredSymbol={false}
        >
          <Row gutter={12}>
            <Col span={12}>
              <FormItem
                label="供应商编码"
                field="supplierCode"
                rules={[{ required: true, message: '请输入' }]}
              >
                <Input placeholder="请输入供应商编码" />
              </FormItem>
            </Col>
            <Col span={12}>
              <FormItem
                label="供应商名称"
                field="supplierName"
                rules={[{ required: true, message: '请输入' }]}
              >
                <Input placeholder="请输入供应商名称" />
              </FormItem>
            </Col>
          </Row>
          <FormItem
            label="统一社会信用代码/税号"
            field="taxNumber"
            rules={[{ required: true, message: '请输入税号' }]}
          >
            <Input placeholder="请输入" />
          </FormItem>
          <FormItem label="地址" field="address">
            <Input placeholder="请输入" />
          </FormItem>
          <Row gutter={12}>
            <Col span={12}>
              <FormItem label="联系电话" field="contactPhone">
                <Input placeholder="请输入" />
              </FormItem>
            </Col>
            <Col span={12}>
              <FormItem label="信用额度" field="creditLimit">
                <InputNumber
                  style={{ width: '100%' }}
                  precision={2}
                  min={0}
                  placeholder="0.00"
                />
              </FormItem>
            </Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}>
              <FormItem label="开户银行" field="bankName">
                <Input placeholder="请输入" />
              </FormItem>
            </Col>
            <Col span={12}>
              <FormItem label="银行账号" field="bankAccount">
                <Input placeholder="请输入" />
              </FormItem>
            </Col>
          </Row>
          <FormItem label="备注" field="remark">
            <Input.TextArea placeholder="请输入" rows={2} />
          </FormItem>
        </Form>
      </Modal>

      <Drawer
        width={720}
        title="供应商详情"
        visible={detailVisible}
        onCancel={() => {
          setDetailVisible(false)
          setCurrentSupplier(null)
        }}
        footer={null}
      >
        {currentSupplier && (
          <div>
            <Descriptions
              column={2}
              title="基本信息"
              style={{ marginBottom: 20 }}
              data={[
                { label: '供应商编码', value: currentSupplier.supplierCode },
                { label: '供应商名称', value: currentSupplier.supplierName },
                { label: '税号', value: currentSupplier.taxNumber },
                {
                  label: '黑名单状态',
                  value: currentSupplier.isBlacklisted ? (
                    <Tag color="red">黑名单</Tag>
                  ) : (
                    <Tag color="green">正常</Tag>
                  ),
                },
                { label: '联系电话', value: currentSupplier.contactPhone || '-' },
                {
                  label: '信用额度',
                  value:
                    currentSupplier.creditLimit !== undefined &&
                    currentSupplier.creditLimit !== null
                      ? `¥${Number(currentSupplier.creditLimit).toFixed(2)}`
                      : '-',
                },
                { label: '地址', value: currentSupplier.address || '-', span: 2 },
                { label: '开户银行', value: currentSupplier.bankName || '-' },
                { label: '银行账号', value: currentSupplier.bankAccount || '-' },
                { label: '备注', value: currentSupplier.remark || '-', span: 2 },
              ]}
            />

            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 12,
              }}
            >
              <h4 style={{ margin: 0 }}>
                <IconFile style={{ marginRight: 4 }} />
                该供应商关联发票（共 {invoices.length} 条）
              </h4>
              <Space>
                {!currentSupplier.isBlacklisted ? (
                  <Button
                    type="primary"
                    status="danger"
                    icon={<IconForbidFill />}
                    onClick={() => setBlacklistReasonVisible('add')}
                  >
                    加入黑名单（冻结所有发票报销）
                  </Button>
                ) : (
                  <Button
                    type="primary"
                    status="success"
                    icon={<IconUnlockFill />}
                    onClick={() => setBlacklistReasonVisible('remove')}
                  >
                    移出黑名单（解冻发票报销）
                  </Button>
                )}
              </Space>
            </div>

            <Table
              size="small"
              rowKey="id"
              columns={invoiceColumns}
              data={invoices}
              pagination={false}
            />
          </div>
        )}
      </Drawer>

      <Modal
        title={
          blacklistReasonVisible === 'add' ? '加入黑名单' : '移出黑名单'
        }
        visible={blacklistReasonVisible !== null}
        onOk={() =>
          blacklistReasonVisible === 'add'
            ? handleAddBlacklist()
            : handleRemoveBlacklist()
        }
        onCancel={() => {
          setBlacklistReasonVisible(null)
          blacklistForm.resetFields()
        }}
        confirmLoading={submitting}
      >
        {blacklistReasonVisible === 'add' && currentSupplier && (
          <div
            style={{
              padding: 12,
              background: '#ffece8',
              borderRadius: 4,
              marginBottom: 16,
              border: '1px solid #ffcdb9',
            }}
          >
            <IconForbidFill style={{ color: '#f53f3f', marginRight: 4 }} />
            将 <b>{currentSupplier.supplierName}</b>{' '}
            加入黑名单后，该供应商所有处于异常状态发票的报销将自动冻结。
          </div>
        )}
        {blacklistReasonVisible === 'remove' && currentSupplier && (
          <div
            style={{
              padding: 12,
              background: '#e8ffea',
              borderRadius: 4,
              marginBottom: 16,
            }}
          >
            <IconUnlockFill style={{ color: '#00b42a', marginRight: 4 }} />
            将 <b>{currentSupplier.supplierName}</b>{' '}
            移出黑名单后，该供应商所有发票的报销将自动解冻。
          </div>
        )}
        <Form
          form={blacklistForm}
          layout="vertical"
          onSubmit={submitBlacklist}
          requiredSymbol={false}
        >
          <FormItem
            label={blacklistReasonVisible === 'add' ? '加入原因' : '移出原因'}
            field="reason"
            rules={[{ required: true, message: '请输入原因' }]}
          >
            <Input.TextArea
              placeholder={
                blacklistReasonVisible === 'add' ? '请输入加入黑名单的原因...' : '请输入移出黑名单的原因...'
              }
              rows={4}
            />
          </FormItem>
        </Form>
      </Modal>
    </div>
  )
}

export default SupplierList
