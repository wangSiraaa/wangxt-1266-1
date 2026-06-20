import React, { useState } from 'react'
import { Form, Input, Button, Message } from '@arco-design/web-react'
import { IconUser, IconLock } from '@arco-design/web-react/icon'
import { useNavigate, useLocation } from 'react-router-dom'
import { authApi, setAuthStorage } from '@/api'
import { useAuth } from '@/store/AuthContext'
import type { LoginResponse } from '@/types'

const FormItem = Form.Item

const Login: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { setUser } = useAuth()
  const [loading, setLoading] = useState(false)
  const [form] = Form.useForm()

  const from = (location.state as any)?.from?.pathname || '/dashboard'

  const handleSubmit = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      const res: LoginResponse = await authApi.login(values.username, values.password)
      setAuthStorage(res.token, res.userInfo)
      setUser(res.userInfo)
      Message.success('登录成功')
      navigate(from, { replace: true })
    } catch (err) {
      // error handled by interceptor
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div className="login-title">
          <h1>企业发票风险协查系统</h1>
          <p>请使用分配的账号登录</p>
        </div>

        <Form
          form={form}
          layout="vertical"
          onSubmit={handleSubmit}
          initialValues={{ username: 'tax01', password: '123456' }}
        >
          <FormItem
            label="用户名"
            field="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<IconUser />} placeholder="请输入用户名" size="large" />
          </FormItem>

          <FormItem
            label="密码"
            field="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password prefix={<IconLock />} placeholder="请输入密码" size="large" />
          </FormItem>

          <FormItem style={{ marginTop: 24 }}>
            <Button type="primary" long size="large" htmlType="submit" loading={loading}>
              登 录
            </Button>
          </FormItem>
        </Form>

        <div style={{ marginTop: 24, padding: 12, background: '#f7f8fa', borderRadius: 4 }}>
          <div style={{ fontSize: 12, color: '#86909c', marginBottom: 8 }}>测试账号（密码均为 123456）：</div>
          <div style={{ fontSize: 12, color: '#4e5969', lineHeight: 1.8 }}>
            <div>税务专员：tax01</div>
            <div>采购负责人：proc01</div>
            <div>财务经理：fin01</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login
