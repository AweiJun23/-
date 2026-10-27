const req = require('../../utils/request.js')

const FARMING_TYPES = ['FERTILIZE', 'IRRIGATE', 'MACHINE', 'PESTICIDE', 'FILM', 'HARVEST', 'PACKAGE', 'TRANSPORT']
const UNITS = ['kg', 'L', 'kWh', 'mu']

Page({
  data: {
    batchId: '',
    farmingTypes: FARMING_TYPES,
    typeIndex: 0,
    materialName: '',
    amount: '',
    units: UNITS,
    unitIndex: 0,
    workDate: '',
    areaMu: '',
    remark: '',
    submitting: false
  },
  onLoad(query) {
    if (query.batchId) this.setData({ batchId: query.batchId })
  },
  onInput(e) {
    const k = e.currentTarget.dataset.k
    this.setData({ [k]: e.detail.value })
  },
  onType(e) { this.setData({ typeIndex: Number(e.detail.value) }) },
  onUnit(e) { this.setData({ unitIndex: Number(e.detail.value) }) },
  onDate(e) { this.setData({ workDate: e.detail.value }) },
  submit() {
    const d = this.data
    if (!d.batchId || !d.amount) {
      return wx.showToast({ title: '请填写批次与用量', icon: 'none' })
    }
    if (!d.workDate) return wx.showToast({ title: '请选择作业日期', icon: 'none' })
    const payload = {
      batchId: Number(d.batchId),
      farmingType: FARMING_TYPES[d.typeIndex],
      materialName: d.materialName,
      amount: Number(d.amount),
      unit: UNITS[d.unitIndex],
      workDate: d.workDate,
      areaMu: d.areaMu ? Number(d.areaMu) : null,
      remark: d.remark
    }
    this.setData({ submitting: true })
    req.post('/api/farming/record', payload)
      .then(() => {
        wx.showToast({ title: '提交成功，已触发碳核算', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1200)
      })
      .catch((e) => wx.showToast({ title: (e && e.message) || '提交失败', icon: 'none' }))
      .finally(() => this.setData({ submitting: false }))
  }
})
