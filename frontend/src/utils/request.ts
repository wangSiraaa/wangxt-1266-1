import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { Message } from '@arco-design/web-react'
import type { Result } from '@/types'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const request: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    if (res.code !== 200) {
      Message.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    const msg = error.response?.data?.message || error.message || '网络错误'
    Message.error(msg)
    return Promise.reject(error)
  }
)

export interface RequestConfig extends AxiosRequestConfig {}

export const httpGet = async <T = any>(url: string, config?: RequestConfig): Promise<T> => {
  const response = await request.get<any, AxiosResponse<Result<T>>>(url, config)
  return response.data.data
}

export const httpPost = async <T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> => {
  const response = await request.post<any, AxiosResponse<Result<T>>>(url, data, config)
  return response.data.data
}

export const httpDelete = async <T = any>(url: string, config?: RequestConfig): Promise<T> => {
  const response = await request.delete<any, AxiosResponse<Result<T>>>(url, config)
  return response.data.data
}

export default request
