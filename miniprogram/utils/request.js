// 轻量请求封装：统一注入 JWT 与 X-Source=MINIAPP，兼容后端 R<> 包装
function qs(obj) {
  if (!obj) return ''
  return Object.keys(obj)
    .map((k) => encodeURIComponent(k) + '=' + encodeURIComponent(obj[k]))
    .join('&')
}

function request(method, path, data) {
  const app = getApp()
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + path,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + (app.globalData.token || ''),
        'X-Source': 'MINIAPP'
      },
      success(res) {
        const d = res.data
        if (d && typeof d === 'object' && 'code' in d) {
          if (d.code === 0 || d.success) return resolve(d.data)
          if (d.code === 401) {
            wx.removeStorageSync('token')
            app.globalData.token = ''
            return reject(new Error(d.message || '未登录'))
          }
          return reject(new Error(d.message || '请求失败'))
        }
        resolve(d)
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

module.exports = {
  get: (p, q) => request('GET', p + (q ? '?' + qs(q) : ''), null),
  post: (p, d) => request('POST', p, d)
}
