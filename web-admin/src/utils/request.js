import axios from 'axios'

const service = axios.create({
  baseURL: '/',
  timeout: 15000
})

// 请求拦截：注入 JWT
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('trace_token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

// 响应拦截：统一拆封 R<> 结构
service.interceptors.response.use(
  (resp) => {
    const data = resp.data
    if (data && typeof data === 'object' && 'code' in data) {
      // 兼容后端 R<> 包装：{code,message,data}
      if (data.code === 0 || data.success) {
        return data.data
      }
      if (data.code === 401) {
        localStorage.removeItem('trace_token')
        window.location.href = '/login'
        return Promise.reject(new Error(data.message || '未登录'))
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    // 未包装（如网关直接透传）
    return data
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('trace_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default service
