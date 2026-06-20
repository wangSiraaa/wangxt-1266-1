import React, { useEffect, useState } from 'react'
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Modal,
  Form,
  Select,
  Input,
  DatePicker,
  Message,
  Table,
  Spin,
  Tabs,
  Timeline,
  Switch as ArcoSwitch,
  Grid,
} from '@arco-design/web-react'
import {
  IconArrowLeft,
  IconFile,
  IconPlus,
  IconCheck,
  IconClose,
  IconUser,
  IconCalendar,
} from '@arco-design/web-react/icon'
import { useNavigate, useParams } from 'react-router-dom'
import dayjs from 'dayjs'
import {
  invoiceApi,
  riskApi,
  approvalApi,
} from '@/api'
import { useAuth } from '@/store/AuthContext'
import {
  RISK_TYPE_OPTIONS,
  MATERIAL_TYPE_OPTIONS,
  getInvoiceStatusColor,
  getInvoiceStatusLabel,
  getRiskTypeLabel,
  getMaterialTypeLabel,
} from '@/utils/enumOptions'
import type {
  InvoiceVO,
  RiskRecordVO,
  RiskMaterialVO,
  ApprovalLogVO,
  RiskTypeCode,
  MaterialTypeCode,
} from '@/types'

const FormItem = Form.Item
const Option = Select.Option
const TabPane = Tabs.TabPane
const Row = Grid.Row
const Col = Grid.Col

const InvoiceDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { hasRole, user } = useAuth()

  const [loading, setLoading] = useState(false)
  const [invoice, setInvoice] = useState<InvoiceVO | null>(null)
  const [riskRecords, setRiskRecords] = useState<RiskRecordVO[]>([])
  const [materials, setMaterials] = useState<RiskMaterialVO[]>([])
  const [approvalLogs, setApprovalLogs] = useState<ApprovalLogVO[]>([])

  const [markRiskVisible, setMarkRiskVisible] = useState(false)
  const [materialVisible, setMaterialVisible] = useState(false)
  const [confirmVisible, setConfirmVisible] = useState(false)
  const [resolveSwitch, setResolveSwitch] = useState(true)

  const [markRiskForm] = Form.useForm()
  const [materialForm] = Form.useForm()
  const [confirmForm] = Form.useForm()
  const [submitting, setSubmitting] = useState(false)

  const invoiceId = Number(id)

  const loadAll = async () => {
    setLoading(true)
    try {
      const [inv, risks, mats, logs] = await Promise.all([
        invoiceApi.getInvoiceById(invoiceId),
        riskApi.getRiskRecords(invoiceId),
        riskApi.getMaterials(invoiceId),
        approvalApi.getLogsByInvoiceId(invoiceId),
      ])
      setInvoice(inv)
      setRiskRecords(risks)
      setMaterials(mats)
      setApprovalLogs(logs)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (invoiceId) loadAll()
  }, [invoiceId])

  const handleMarkRisk = async (values: any) => {
    setSubmitting(true)
    try {
      await riskApi.markRisk({
        invoiceId,
        riskType: values.riskType,
        riskDescription: values.riskDescription,
        markReason: values.markReason,
      })
      Message.success('风险标记成功')
      setMarkRiskVisible(false)
      markRiskForm.resetFields()
      loadAll()
    } finally {
      setSubmitting(false)
    }
  }

  const hasContract = materials.some((m) => m.materialType === 'CONTRACT')

  const handleSupplementMaterial = async (values: any) => {
    setSubmitting(true)
    try {
      await riskApi.supplementMaterial({
        invoiceId,
        materialType: values.materialType,
        materialName: values.materialName,
        materialUrl: values.materialUrl,
        contractNumber: values.contractNumber,
        contractDate: values.contractDate
          ? dayjs(values.contractDate).format('YYYY-MM-DD')
          : undefined,
        deliveryNoteNumber: values.deliveryNoteNumber,
        deliveryDate: values.deliveryDate
          ? dayjs(values.deliveryDate).format('YYYY-MM-DD')
          : undefined,
        remark: values.remark,
      })
      Message.success('材料补充成功')
      setMaterialVisible(false)
      materialForm.resetFields()
      loadAll()
    } finally {
      setSubmitting(false)
    }
  }

  const handleConfirm = async (values: any) => {
    setSubmitting(true)
    try {
      await riskApi.confirmConclusion({
        invoiceId,
        resolved: resolveSwitch,
        conclusion: values.conclusion,
      })
      Message.success(resolveSwitch ? '已确认解除风险，结论不可删除' : '已确认风险异常，报销已冻结，结论不可删除')
      setConfirmVisible(false)
      confirmForm.resetFields()
      loadAll()
    } finally {
      setSubmitting(false)
    }
  }

  const riskColumns = [
    {
      title: '风险类型',
      dataIndex: 'riskType',
      width: 180,
      render: (v: RiskTypeCode) => (
        <Tag color="orange">{getRiskTypeLabel(v)}</Tag>
      ),
    },
    { title: '风险描述', dataIndex: 'riskDescription', ellipsis: true },
    { title: '标记原因', dataIndex: 'markReason', ellipsis: true },
    {
      title: '标记人',
      dataIndex: 'markedByName',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'isResolved',
      width: 100,
      render: (v: boolean) =>
        v ? <Tag color="green">已解除</Tag> : <Tag color="red">未解除</Tag>,
    },
    { title: '解除说明', dataIndex: 'resolveDescription', ellipsis: true },
    {
      title: '解除人',
      dataIndex: 'resolvedByName',
      width: 120,
      render: (v: string) => v || '-',
    },
    {
      title: '标记时间',
      dataIndex: 'createdAt',
      width: 170,
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
  ]

  const materialColumns = [
    {
      title: '材料类型',
      dataIndex: 'materialType',
      width: 140,
      render: (v: MaterialTypeCode) => (
        <Tag color="blue">{getMaterialTypeLabel(v)}</Tag>
      ),
    },
    { title: '材料名称', dataIndex: 'materialName', width: 200 },
    {
      title: '合同编号',
      dataIndex: 'contractNumber',
      width: 160,
      render: (v: string) => v || '-',
    },
    {
      title: '合同日期',
      dataIndex: 'contractDate',
      width: 120,
      render: (v: string) => (v ? dayjs(v).format('YYYY-MM-DD') : '-'),
    },
    {
      title: '收货单号',
      dataIndex: 'deliveryNoteNumber',
      width: 160,
      render: (v: string) => v || '-',
    },
    {
      title: '收货日期',
      dataIndex: 'deliveryDate',
      width: 120,
      render: (v: string) => (v ? dayjs(v).format('YYYY-MM-DD') : '-'),
    },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    {
      title: '上传人',
      dataIndex: 'uploadedByName',
      width: 120,
    },
    {
      title: '上传时间',
      dataIndex: 'createdAt',
      width: 170,
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
  ]

  return (
    <Spin loading={loading} style={{ width: '100%' }}>
      {invoice && (
        <div>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 16,
            }}
          >
            <Space>
              <Button icon={<IconArrowLeft />} onClick={() => navigate(-1)}>
                返回
              </Button>
              <h2 style={{ margin: 0 }}>发票详情</h2>
              <Tag color={getInvoiceStatusColor(invoice.status)}>
                {getInvoiceStatusLabel(invoice.status)}
              </Tag>
              {invoice.reimbursementFrozen && <Tag color="red">报销已冻结</Tag>}
              {!invoice.conclusionDeletable && (
                <Tag color="arcoblue">结论不可删除</Tag>
              )}
            </Space>
            <Space>
              {hasRole(['TAX_SPECIALIST']) &&
                (invoice.status === 'NORMAL' ||
                  invoice.status === 'PENDING_REVIEW' ||
                  invoice.conclusionDeletable) && (
                  <Button
                    type="primary"
                    status="warning"
                    onClick={() => setMarkRiskVisible(true)}
                  >
                    标记风险
                  </Button>
                )}
              {hasRole(['PROCUREMENT_HEAD']) &&
                (invoice.status === 'RISK_IDENTIFIED' ||
                  invoice.status === 'MATERIALS_SUPPLEMENTED') && (
                  <Button
                    type="primary"
                    icon={<IconPlus />}
                    onClick={() => setMaterialVisible(true)}
                  >
                    补充材料
                  </Button>
                )}
              {hasRole(['FINANCE_MANAGER']) &&
                (invoice.status === 'RISK_IDENTIFIED' ||
                  invoice.status === 'MATERIALS_SUPPLEMENTED' ||
                  invoice.status === 'PENDING_REVIEW' ||
                  invoice.status === 'PENDING_CONFIRM') && (
                  <Button
                    type="primary"
                    onClick={() => setConfirmVisible(true)}
                  >
                    确认处理结论
                  </Button>
                )}
            </Space>
          </div>

          <Card style={{ marginBottom: 16 }}>
            <Descriptions
              column={3}
              title={
                <Space>
                  <IconFile /> 发票基本信息
                </Space>
              }
              data={[
                { label: '发票代码', value: invoice.invoiceCode },
                { label: '发票号码', value: invoice.invoiceNumber },
                { label: '发票类型', value: invoice.invoiceType },
                {
                  label: '开票日期',
                  value: dayjs(invoice.invoiceDate).format('YYYY-MM-DD'),
                },
                {
                  label: '税前金额',
                  value: `¥${Number(invoice.amountBeforeTax).toFixed(2)}`,
                },
                {
                  label: '税额',
                  value: `¥${Number(invoice.taxAmount).toFixed(2)}`,
                },
                {
                  label: '价税合计',
                  value: <b style={{ color: '#f53f3f' }}>{`¥${Number(
                    invoice.totalAmount
                  ).toFixed(2)}`}</b>,
                },
                {
                  label: '税率',
                  value: invoice.taxRate ? `${(invoice.taxRate * 100).toFixed(2)}%` : '-',
                },
                { label: '购方名称', value: invoice.buyerName || '-' },
                { label: '购方税号', value: invoice.buyerTaxNumber || '-' },
                { label: '销售方', value: invoice.supplierName },
                { label: '销售方编码', value: invoice.supplierCode },
                { label: '货物描述', value: invoice.goodsDescription || '-', span: 3 },
                { label: '备注', value: invoice.remark || '-', span: 3 },
              ]}
              style={{ padding: 12 }}
            />

            {(invoice.conclusion || invoice.confirmedByName) && (
              <div
                style={{
                  marginTop: 16,
                  padding: 16,
                  background:
                    invoice.status === 'RESOLVED' ? '#e8ffea' : '#ffece8',
                  borderRadius: 6,
                  borderLeft: `4px solid ${
                    invoice.status === 'RESOLVED' ? '#00b42a' : '#f53f3f'
                  }`,
                }}
              >
                <div style={{ fontWeight: 600, marginBottom: 8 }}>
                  {invoice.status === 'RESOLVED' ? (
                    <Space>
                      <IconCheck style={{ color: '#00b42a' }} />
                      处理结论：风险已解除
                    </Space>
                  ) : (
                    <Space>
                      <IconClose style={{ color: '#f53f3f' }} />
                      处理结论：风险确认异常
                    </Space>
                  )}
                  {!invoice.conclusionDeletable && (
                    <Tag color="arcoblue" style={{ marginLeft: 8 }}>
                      不可删除
                    </Tag>
                  )}
                </div>
                <div style={{ marginBottom: 8 }}>
                  <b>结论：</b>
                  {invoice.conclusion}
                </div>
                <Space size="large" style={{ color: '#86909c', fontSize: 13 }}>
                  <Space>
                    <IconUser />
                    确认人：{invoice.confirmedByName || '-'}
                  </Space>
                  <Space>
                    <IconCalendar />
                    确认时间：
                    {invoice.confirmedAt
                      ? dayjs(invoice.confirmedAt).format('YYYY-MM-DD HH:mm')
                      : '-'}
                  </Space>
                </Space>
              </div>
            )}
          </Card>

          <Card>
            <Tabs defaultActiveTab="risks">
              <TabPane key="risks" title={`风险记录（${riskRecords.length}）`}>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: 12,
                  }}
                >
                  {riskRecords.some((r) => !r.isResolved) && (
                    <Tag color="red">存在未解除的风险</Tag>
                  )}
                </div>
                <Table
                  rowKey="id"
                  columns={riskColumns}
                  data={riskRecords}
                  pagination={false}
                />
              </TabPane>

              <TabPane key="materials" title={`补充材料（${materials.length}）`}>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: 12,
                  }}
                >
                  {hasContract ? (
                    <Tag color="green">
                      <IconCheck /> 已上传采购合同
                    </Tag>
                  ) : (
                    <Tag color="red">
                      <IconClose /> 缺少采购合同（解除风险前必须补充）
                    </Tag>
                  )}
                  {hasRole(['PROCUREMENT_HEAD']) &&
                    (invoice.status === 'RISK_IDENTIFIED' ||
                      invoice.status === 'MATERIALS_SUPPLEMENTED') && (
                      <Button
                        type="primary"
                        size="small"
                        icon={<IconPlus />}
                        onClick={() => setMaterialVisible(true)}
                      >
                        补充材料
                      </Button>
                    )}
                </div>
                <Table
                  rowKey="id"
                  columns={materialColumns}
                  data={materials}
                  pagination={false}
                />
              </TabPane>

              <TabPane key="logs" title={`审批日志（${approvalLogs.length}）`}>
                <Timeline
                  style={{ padding: '20px 0 0 20px' }}
                  items={approvalLogs.map((log) => ({
                    dotColor:
                      log.action === 'CONFIRM_RESOLVED'
                        ? 'green'
                        : log.action === 'CONFIRM_REJECTED' ||
                          log.action === 'FREEZE_REIMBURSEMENT' ||
                          log.action === 'ADD_BLACKLIST'
                        ? 'red'
                        : 'blue',
                    children: (
                      <div style={{ marginBottom: 16 }}>
                        <div style={{ fontWeight: 500 }}>
                          <Tag color="arcoblue">{log.actionDescription}</Tag>
                          <span style={{ marginLeft: 8 }}>{log.operatorName}</span>
                          <Tag color="purple" style={{ marginLeft: 8 }}>
                            {log.operatorRoleDescription || log.operatorRole}
                          </Tag>
                          <span style={{ marginLeft: 8, color: '#86909c', fontSize: 12 }}>
                            {dayjs(log.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                          </span>
                        </div>
                        {log.remark && (
                          <div
                            style={{
                              marginTop: 4,
                              padding: 8,
                              background: '#f7f8fa',
                              borderRadius: 4,
                              color: '#4e5969',
                            }}
                          >
                            {log.remark}
                          </div>
                        )}
                      </div>
                    ),
                  }))}
                />
                {approvalLogs.length === 0 && (
                  <div
                    style={{
                      textAlign: 'center',
                      padding: 40,
                      color: '#86909c',
                    }}
                  >
                    暂无审批日志
                  </div>
                )}
              </TabPane>
            </Tabs>
          </Card>
        </div>
      )}

      <Modal
        title="标记风险"
        visible={markRiskVisible}
        onOk={() => markRiskForm.submit()}
        onCancel={() => {
          setMarkRiskVisible(false)
          markRiskForm.resetFields()
        }}
        confirmLoading={submitting}
      >
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
            <Select placeholder="请选择风险类型">
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

      <Modal
        title="补充材料"
        visible={materialVisible}
        onOk={() => materialForm.submit()}
        onCancel={() => {
          setMaterialVisible(false)
          materialForm.resetFields()
        }}
        confirmLoading={submitting}
        style={{ width: 560 }}
      >
        <Form
          form={materialForm}
          layout="vertical"
          onSubmit={handleSupplementMaterial}
          requiredSymbol={false}
        >
          <FormItem
            label="材料类型"
            field="materialType"
            rules={[{ required: true, message: '请选择材料类型' }]}
            extra={
              !hasContract ? (
                <span style={{ color: '#f53f3f' }}>建议优先上传「采购合同」</span>
              ) : null
            }
          >
            <Select placeholder="请选择材料类型">
              {MATERIAL_TYPE_OPTIONS.map((opt) => (
                <Option key={opt.value} value={opt.value}>
                  {opt.label}
                </Option>
              ))}
            </Select>
          </FormItem>
          <FormItem label="材料名称" field="materialName">
            <Input placeholder="请输入材料名称" />
          </FormItem>
          <FormItem label="附件链接" field="materialUrl">
            <Input placeholder="请输入材料文件URL（可选）" />
          </FormItem>
          <Row gutter={12}>
            <Col span={12}>
              <FormItem label="合同编号" field="contractNumber">
                <Input placeholder="合同类材料请填写" />
              </FormItem>
            </Col>
            <Col span={12}>
              <FormItem label="合同日期" field="contractDate">
                <DatePicker style={{ width: '100%' }} />
              </FormItem>
            </Col>
          </Row>
          <Row gutter={12}>
            <Col span={12}>
              <FormItem label="收货单号" field="deliveryNoteNumber">
                <Input placeholder="收货类材料请填写" />
              </FormItem>
            </Col>
            <Col span={12}>
              <FormItem label="收货日期" field="deliveryDate">
                <DatePicker style={{ width: '100%' }} />
              </FormItem>
            </Col>
          </Row>
          <FormItem label="备注" field="remark">
            <Input.TextArea placeholder="请输入备注说明" rows={2} />
          </FormItem>
        </Form>
      </Modal>

      <Modal
        title="确认处理结论"
        visible={confirmVisible}
        onOk={() => confirmForm.submit()}
        onCancel={() => {
          setConfirmVisible(false)
          confirmForm.resetFields()
        }}
        confirmLoading={submitting}
        style={{ width: 560 }}
      >
        <div style={{ marginBottom: 16 }}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 16,
              padding: 12,
              background: '#f7f8fa',
              borderRadius: 4,
            }}
          >
            <span>
              <b>处理结果：</b>
            </span>
            <Space>
              <span>{resolveSwitch ? '解除风险' : '确认异常/冻结报销'}</span>
              <ArcoSwitch
                checked={resolveSwitch}
                onChange={setResolveSwitch}
                checkedText={<IconCheck />}
                uncheckedText={<IconClose />}
              />
            </Space>
          </div>

          {resolveSwitch && !hasContract && (
            <div
              style={{
                padding: 12,
                background: '#ffece8',
                color: '#f53f3f',
                borderRadius: 4,
                marginBottom: 12,
                border: '1px solid #ffcdb9',
              }}
            >
              <IconClose style={{ marginRight: 4 }} />
              该发票尚未上传采购合同，无法解除风险。请先让采购负责人补充合同材料。
            </div>
          )}

          {resolveSwitch && (
            <div style={{ padding: 12, background: '#e8ffea', borderRadius: 4, marginBottom: 12 }}>
              <IconCheck style={{ color: '#00b42a', marginRight: 4 }} />
              选择「解除风险」将把发票状态改为正常，并自动解冻报销。
            </div>
          )}

          {!resolveSwitch && (
            <div style={{ padding: 12, background: '#ffece8', borderRadius: 4, marginBottom: 12 }}>
              <IconClose style={{ color: '#f53f3f', marginRight: 4 }} />
              选择「确认异常」将冻结该发票的报销，状态改为「风险确认（异常）」。
            </div>
          )}

          <div
            style={{
              padding: 12,
              background: '#e8f3ff',
              borderRadius: 4,
              marginBottom: 12,
              border: '1px dashed #165dff',
            }}
          >
            <b style={{ color: '#165dff' }}>重要提示：</b>
            处理结论一经确认，将形成<span style={{ color: '#f53f3f' }}>不可删除</span>的历史记录。
          </div>
        </div>

        <Form
          form={confirmForm}
          layout="vertical"
          onSubmit={handleConfirm}
          requiredSymbol={false}
        >
          <FormItem
            label="处理结论"
            field="conclusion"
            rules={[{ required: true, message: '请输入处理结论' }]}
          >
            <Input.TextArea
              placeholder={
                resolveSwitch
                  ? '请详细说明解除风险的依据...'
                  : '请详细说明确认异常或冻结报销的原因...'
              }
              rows={5}
            />
          </FormItem>
        </Form>
      </Modal>
    </Spin>
  )
}

export default InvoiceDetail
