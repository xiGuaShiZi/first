import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/api',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  if (config.url?.startsWith('/auth/')) return config
  const isUserApi = config.url?.startsWith('/user/')
  const isMerchantApi = config.url?.startsWith('/merchant/')
  let token = null
  if (isUserApi) {
    token = localStorage.getItem('user_token')
  } else if (isMerchantApi) {
    token = localStorage.getItem('merchant_token')
  } else {
    token = localStorage.getItem('admin_token')
  }
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const payload = error.response?.data || { message: '网络请求失败，请稍后重试' }
    if (error.response?.status === 401) {
      const isUserApi = error.config?.url?.startsWith('/user/')
      const isMerchantApi = error.config?.url?.startsWith('/merchant/')
      if (isUserApi) {
        localStorage.removeItem('user_token')
        localStorage.removeItem('user_name')
        window.dispatchEvent(new Event('user-auth-changed'))
      } else if (isMerchantApi) {
        localStorage.removeItem('merchant_token')
        localStorage.removeItem('merchant_name')
        window.dispatchEvent(new Event('merchant-auth-changed'))
      } else {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_name')
        window.dispatchEvent(new Event('admin-auth-changed'))
      }
    }
    ElMessage.error(payload.message || '操作失败')
    return Promise.reject(payload)
  }
)

export default request
