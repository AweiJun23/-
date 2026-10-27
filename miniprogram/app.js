App({
  globalData: {
    // 本地联调用网关地址（微信正式环境需 https 并配置服务器合法域名）
    baseUrl: 'http://localhost:9000',
    token: '',
    user: null
  },
  onLaunch() {
    const token = wx.getStorageSync('token')
    const user = wx.getStorageSync('user')
    if (token) this.globalData.token = token
    if (user) this.globalData.user = user
  }
})
