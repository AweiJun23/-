const req = require('../../utils/request.js')

Page({
  data: {
    batchNo: '',
    trace: null,
    batchId: '',
    subsidy: null,
    loading: false
  },
  onInput(e) { this.setData({ batchNo: e.detail.value }) },
  async load() {
    if (!this.data.batchNo) return wx.showToast({ title: '请输入批次编号', icon: 'none' })
    this.setData({ loading: true })
    try {
      const trace = await req.get('/api/batch/trace/' + this.data.batchNo)
      this.setData({ trace, batchId: trace.id || '' })
    } catch (e) {
      this.setData({ trace: null })
      wx.showToast({ title: (e && e.message) || '查询失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  async loadSubsidy() {
    if (!this.data.batchId) return
    try {
      const r = await req.get('/api/report/latest/' + this.data.batchId)
      this.setData({ subsidy: r })
    } catch (e) {
      wx.showToast({ title: '暂无补贴材料', icon: 'none' })
    }
  }
})
