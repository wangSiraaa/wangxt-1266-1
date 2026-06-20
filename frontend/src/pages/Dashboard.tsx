import React, { useEffect, useState } from 'react'
import { Card, Statistic, Grid, Spin, Tag, Button } from '@arco-design/web-react'
import {
  IconFile,
  IconCheckCircle,
  IconExclamationCircle,
  IconClockCircle,
  IconUser,
  IconForbid,
} from '@arco-design/web-react/icon'
import { useNavigate } from 'react-router-dom'
import { dashboardApi } from '@/api'
import type { DashboardVO } from '@/types'

const Row = Grid.Row
const Col = Grid.Col

const Dashboard: React.FC = () => {
  const navigate = useNavigate()
  const [data, setData] = useState<DashboardVO | null>(null)
  const [loading, setLoading] = useState(false)

  const loadData = async () => {
    setLoading(true)
    try {
      const res = await dashboardApi.getDashboard()
      setData(res)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const cards = data
    ? [
        {
          title: '发票总数',
          value: data.totalInvoices,
          icon: <IconFile style={{ fontSize: 28, color: '#165dff' }} />,
          color: '#e8f3ff',
          tag: null,
        },
        {
          title: '正常发票',
          value: data.normalInvoices,
          icon: <IconCheckCircle style={{ fontSize: 28, color: '#00b42a' }} />,
          color: '#e8ffea',
          tag: <Tag color="green">正常</Tag>,
        },
        {
          title: '待审核',
          value: data.pendingReviewInvoices,
          icon: <IconClockCircle style={{ fontSize: 28, color: '#ff7d00' }} />,
          color: '#fff7e8',
          tag: <Tag color="orange">待处理</Tag>,
        },
        {
          title: '已标记风险',
          value: data.riskIdentifiedInvoices,
          icon: <IconExclamationCircle style={{ fontSize: 28, color: '#f53f3f' }} />,
          color: '#ffece8',
          tag: <Tag color="red">需关注</Tag>,
        },
        {
          title: '材料已补充',
          value: data.materialsSupplementedInvoices,
          icon: <IconFile style={{ fontSize: 28, color: '#722ed1' }} />,
          color: '#f9f0ff',
          tag: <Tag color="purple">待确认</Tag>,
        },
        {
          title: '待经理确认',
          value: data.pendingConfirmInvoices,
          icon: <IconUser style={{ fontSize: 28, color: '#14c9c9' }} />,
          color: '#e8fffb',
          tag: <Tag color="cyan">审批中</Tag>,
        },
        {
          title: '风险解除',
          value: data.resolvedInvoices,
          icon: <IconCheckCircle style={{ fontSize: 28, color: '#00b42a' }} />,
          color: '#e8ffea',
          tag: <Tag color="green">通过</Tag>,
        },
        {
          title: '风险确认异常',
          value: data.rejectedInvoices,
          icon: <IconForbid style={{ fontSize: 28, color: '#f53f3f' }} />,
          color: '#ffece8',
          tag: <Tag color="red">异常</Tag>,
        },
        {
          title: '供应商总数',
          value: data.totalSuppliers,
          icon: <IconUser style={{ fontSize: 28, color: '#165dff' }} />,
          color: '#e8f3ff',
          tag: null,
        },
        {
          title: '黑名单供应商',
          value: data.blacklistedSuppliers,
          icon: <IconForbid style={{ fontSize: 28, color: '#f53f3f' }} />,
          color: '#ffece8',
          tag: <Tag color="red">高风险</Tag>,
        },
        {
          title: '未解除风险数',
          value: data.pendingRisks,
          icon: <IconExclamationCircle style={{ fontSize: 28, color: '#ff7d00' }} />,
          color: '#fff7e8',
          tag: <Tag color="orange">处理中</Tag>,
        },
      ]
    : []

  return (
    <Spin loading={loading} style={{ width: '100%' }}>
      <div>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 20,
          }}
        >
          <div>
            <h2 style={{ margin: 0, color: '#1d2129' }}>首页概览</h2>
            <div style={{ color: '#86909c', marginTop: 4 }}>
              实时展示发票风险协查数据统计
            </div>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <Button onClick={() => navigate('/invoices')}>进入发票管理</Button>
            <Button type="primary" onClick={() => navigate('/suppliers')}>
              供应商管理
            </Button>
          </div>
        </div>

        <Row gutter={16}>
          {cards.map((card, index) => (
            <Col span={8} key={index} style={{ marginBottom: 16 }}>
              <Card bordered hoverable>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ color: '#86909c', fontSize: 14, marginBottom: 8 }}>
                      {card.title}
                    </div>
                    <div
                      style={{
                        fontSize: 32,
                        fontWeight: 600,
                        color: '#1d2129',
                        display: 'flex',
                        alignItems: 'baseline',
                        gap: 8,
                      }}
                    >
                      {card.value}
                      {card.tag}
                    </div>
                  </div>
                  <div
                    style={{
                      width: 56,
                      height: 56,
                      borderRadius: 12,
                      background: card.color,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {card.icon}
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </Spin>
  )
}

export default Dashboard
