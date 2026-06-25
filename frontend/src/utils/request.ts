import axios, { type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 取消重复请求
const pendingMap = new Map<string, AbortController>()

const getRequestKey = (config: AxiosRequestConfig): string => {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

const addPending = (config: InternalAxiosRequestConfig) => {
  const key = getRequestKey(config)
  if (pendingMap.has(key)) {
    pendingMap.get(key)!.abort()
    pendingMap.delete(key)
  }
  const controller = new AbortController()
  config.signal = controller.signal
  pendingMap.set(key, controller)
}

const removePending = (config: AxiosRequestConfig) => {
  const key = getRequestKey(config)
  pendingMap.delete(key)
}

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    addPending(config)
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    removePending(response.config)
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (axios.isCancel(error)) {
      console.log('重复请求已取消:', error.message)
      return Promise.reject(error)
    }
    removePending(error.config || {})
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限访问')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

export default instance
