import React from 'react'
import { Layout, Menu, Avatar, Dropdown } from '@arco-design/web-react'
import {
  IconDashboard,
  IconFile,
  IconUser,
  IconSettings,
  IconNotification,
  IconPoweroff,
  IconStickynote,
  IconCustomerService,
} from '@arco-design/web-react/icon'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/store/AuthContext'
import { clearAuthStorage } from '@/api'

const MenuItem = Menu.Item
const { Header, Sider, Content } = Layout

const MainLayout: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()

  const menuMap: Record<string, string> = {
    '/dashboard': 'dashboard',
    '/invoices': 'invoices',
    '/suppliers': 'suppliers',
    '/approval-logs': 'approval',
  }

  const selectedKeys = [menuMap[location.pathname] || 'dashboard']

  const handleLogout = () => {
    clearAuthStorage()
    logout()
    navigate('/login', { replace: true })
  }

  const dropList = [
    {
      key: 'logout',
      content: '退出登录',
      icon: <IconPoweroff />,
      onClick: handleLogout,
    },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          height: 60,
          background: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0 24px',
          boxShadow: '0 1px 2px rgba(0,0,0,0.06)',
          zIndex: 10,
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div
            style={{
              width: 36,
              height: 36,
              background: 'linear-gradient(135deg, #667eea, #764ba2)',
              borderRadius: 8,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              fontWeight: 700,
              fontSize: 18,
            }}
          >
            票
          </div>
          <div style={{ fontSize: 18, fontWeight: 600, color: '#1d2129' }}>
            企业发票风险协查系统
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
          {user && (
            <>
              <span
                style={{
                  padding: '4px 12px',
                  background: '#e8f3ff',
                  color: '#165dff',
                  borderRadius: 12,
                  fontSize: 13,
                }}
              >
                {user.roleDescription}
              </span>
              <Dropdown droplist={dropList} position="br">
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                  <Avatar size={36} style={{ backgroundColor: '#165dff' }}>
                    {user.realName?.charAt(0)}
                  </Avatar>
                  <span style={{ color: '#1d2129' }}>{user.realName}</span>
                </div>
              </Dropdown>
            </>
          )}
        </div>
      </Header>

      <Layout>
        <Sider width={220} style={{ background: '#fff', borderRight: '1px solid #e5e6eb' }}>
          <Menu
            style={{ width: 220, height: 'calc(100vh - 60px)', border: 'none', paddingTop: 8 }}
            selectedKeys={selectedKeys}
            onClickMenuItem={(key) => {
              const paths: Record<string, string> = {
                dashboard: '/dashboard',
                invoices: '/invoices',
                suppliers: '/suppliers',
                approval: '/approval-logs',
              }
              navigate(paths[key])
            }}
          >
            <MenuItem key="dashboard">
              <IconDashboard style={{ marginRight: 8 }} />
              首页概览
            </MenuItem>
            <MenuItem key="invoices">
              <IconFile style={{ marginRight: 8 }} />
              发票管理
            </MenuItem>
            <MenuItem key="suppliers">
              <IconCustomerService style={{ marginRight: 8 }} />
              供应商管理
            </MenuItem>
            <MenuItem key="approval">
              <IconStickynote style={{ marginRight: 8 }} />
              审批日志
            </MenuItem>
          </Menu>
        </Sider>

        <Layout style={{ background: '#f2f3f5' }}>
          <Content style={{ padding: 20, minHeight: 'calc(100vh - 60px)' }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </Layout>
  )
}

export default MainLayout
