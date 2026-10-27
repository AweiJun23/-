import axios from 'axios'

const service = axios.create({ baseURL: '/', timeout: 15000 })

service.interceptors.response.use(
  (resp) => {
    const d = resp.data
    if (d && typeof d === 'object' && 'code' in d) {
      if (d.code === 0 || d.success) return d.data
      return Promise.reject(new Error(d.message || '请求失败'))
    }
    return d
  },
  (err) => Promise.reject(err)
)

export const batchApi = {
  // 公开溯源聚合（网关已放行 /api/batch/trace/**）
  trace: (batchNo) => service.get(`/api/batch/trace/${batchNo}`)
}

export const chainApi = {
  verify: (data) => service.post('/api/chain/verify', data)
}

export default service
