import request from '../utils/request'

export const authApi = {
  login: (data) => request.post('/api/auth/login', data),
  register: (data) => request.post('/api/auth/register', data),
  deleteFarmer: (id) => request.delete(`/api/auth/farmers/${id}`)
}

export const batchApi = {
  // 收购商创建批次
  create: (data) => request.post('/api/batch/create', data),
  // 列表（按合作社/农户）
  list: (params) => request.get('/api/batch/list', { params }),
  detail: (id) => request.get(`/api/batch/${id}`),
  byNo: (batchNo) => request.get(`/api/batch/no/${batchNo}`),
  // 溯源聚合（公开，用于扫码/核验）
  trace: (batchNo) => request.get(`/api/batch/trace/${batchNo}`),
  // 二维码（Base64 data URL，JSON 传输，规避 <img> 直连二进制经代理的兼容问题）
  qrBase64: (batchNo) => request.get(`/api/batch/qr-base64/${batchNo}`),
  qr: (batchNo) => `/api/batch/qr/${batchNo}`
}

export const farmerApi = {
  list: (params) => request.get('/api/auth/farmers', { params })
}

export const carbonApi = {
  report: (batchId) => request.get(`/api/carbon/report/${batchId}`),
  reportByNo: (batchNo) => request.get(`/api/carbon/report/no/${batchNo}`)
}

export const reportApi = {
  generate: (batchId) => request.post('/api/report/subsidy/generate', null, { params: { batchId } }),
  latest: (batchId) => request.get(`/api/report/subsidy/latest/${batchId}`)
}

export const chainApi = {
  // 公开验真
  verify: (data) => request.post('/api/chain/verify', data)
}
