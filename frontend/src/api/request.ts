import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 60000,
})

request.interceptors.response.use(
  (response) => {
    const { code, data, message } = response.data as {
      code: number
      data: unknown
      message: string
    }
    if (code === 0) {
      return data as any
    }
    if (code === 40100) {
      useUserStore().clearLoginState()
      ElMessage({ message: '登录过期，请重新登录', type: 'warning', duration: 1500 })
      setTimeout(() => router.push('/login'), 1500)
    }
    return Promise.reject(new Error(message || '请求失败'))
  },
  () => {
    return Promise.reject(new Error('网络异常，请稍后重试'))
  },
)

export default request
