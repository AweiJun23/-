const req = require('../../utils/request.js')

Page({
  data: { logged: false, user: null },
  onShow() {
    const app = getApp()
    this.setData({
      logged: !!app.globalData.token,
      user: app.globalData.user || null
    })
  },
  wechatLogin() {
    const that = this
    wx.login({
      success(res) {
        if (!res.code) return wx.showToast({ title: '微信登录失败', icon: 'none' })
        req.post('/api/auth/wechat/login', { code: res.code })
          .then((data) => {
            const app = getApp()
            app.globalData.token = data.token
            app.globalData.user = data
            wx.setStorageSync('token', data.token)
            wx.setStorageSync('user', data)
            that.setData({ logged: true, user: data })
            wx.showToast({ title: '登录成功', icon: 'success' })
          })
          .catch((e) => wx.showToast({ title: (e && e.message) || '登录失败', icon: 'none' }))
      }
    })
  },
  goFarming() {
    wx.navigateTo({ url: '/pages/farming/farming' })
  },
  goReport() {
    wx.navigateTo({ url: '/pages/report/report' })
  }
})
