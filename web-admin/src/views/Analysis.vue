<template>
  <div>
    <h2 style="margin:0 0 16px;display:flex;align-items:center;gap:12px">
      数据分析
      <el-button size="small" :loading="loading" @click="load">重新加载</el-button>
    </h2>

    <!-- 概览卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="s in stats" :key="s.label">
        <el-card shadow="hover" style="text-align:center">
          <div style="font-size:26px;font-weight:700;color:#07c160">{{ s.value }}</div>
          <div style="color:#909399;font-size:13px;margin-top:4px">{{ s.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="loading" style="text-align:center;padding:40px;color:#909399">数据加载中…</el-card>
    <el-card v-else-if="err" style="text-align:center;padding:24px;color:#f56c6c">
      ⚠️ 加载出错：{{ err }}<br>
      <span style="color:#909399;font-size:12px">请确认已登录且网络正常，然后点上方「重新加载」。</span>
    </el-card>
    <el-card v-else-if="!carbons.length" style="text-align:center;padding:24px;color:#909399">
      暂无碳足迹数据，请先在「批次管理」生成碳报告后再查看分析。<br>
      <span style="color:#c0c4cc;font-size:12px">诊断：批次 {{ debug.batches }} 个，尝试拉取碳报告 {{ debug.tried }} 个，成功 {{ debug.ok }} 个<span v-if="debug.firstErr">，首个错误：{{ debug.firstErr }}</span></span>
    </el-card>

    <template v-else>
      <!-- 折线图 -->
      <el-card style="margin-bottom:16px">
        <template #header><b>各示范批次碳足迹趋势（折线图）</b><span style="color:#909399;font-size:12px">　单位 kgCO₂e</span></template>
        <svg :viewBox="'0 0 640 280'" style="width:100%;height:300px">
          <line v-for="t in lineYTicks" :key="'ly'+t.y" :x1="padL" :y1="t.y" :x2="640-padR" :y2="t.y" stroke="#eef0f2" stroke-width="1" />
          <text v-for="t in lineYTicks" :key="'lyt'+t.y" :x="padL-8" :y="t.y+4" text-anchor="end" font-size="11" fill="#909399">{{ t.label }}</text>
          <polyline :points="linePoints" fill="none" stroke="#07c160" stroke-width="2.5" />
          <circle v-for="(d,i) in lineDots" :key="'d'+i" :cx="d.x" :cy="d.y" r="3.5" fill="#07c160" />
          <text v-for="(d,i) in lineDots" :key="'dt'+i" v-show="i % 3 === 0" :x="d.x" :y="280-padB+16" text-anchor="middle" font-size="10" fill="#909399">{{ d.label }}</text>
          <text :x="padL" :y="16" font-size="11" fill="#c0c4cc">总排放</text>
        </svg>
      </el-card>

      <el-row :gutter="16">
        <!-- 柱状图 -->
        <el-col :span="12">
          <el-card style="margin-bottom:16px">
            <template #header><b>各批次单位碳足迹（柱状图）</b><span style="color:#909399;font-size:12px">　kgCO₂e/kg</span></template>
            <svg :viewBox="'0 0 320 280'" style="width:100%;height:300px">
              <line v-for="t in barYTicks" :key="'by'+t.y" :x1="padL" :y1="t.y" :x2="320-padR" :y2="t.y" stroke="#eef0f2" stroke-width="1" />
              <text v-for="t in barYTicks" :key="'byt'+t.y" :x="padL-6" :y="t.y+4" text-anchor="end" font-size="10" fill="#909399">{{ t.label }}</text>
              <g v-for="(b,i) in barRects" :key="'b'+i">
                <rect :x="b.x" :y="b.y" :width="b.w" :height="b.h" rx="2" fill="#409eff" />
                <text :x="b.x + b.w/2" :y="b.y - 4" text-anchor="middle" font-size="9" fill="#606266">{{ b.valText }}</text>
                <text v-if="i % 2 === 0" :x="b.x + b.w/2" :y="280-padB+14" text-anchor="middle" font-size="9" fill="#909399">{{ b.label }}</text>
              </g>
            </svg>
          </el-card>
        </el-col>

        <!-- 扇形图 -->
        <el-col :span="12">
          <el-card style="margin-bottom:16px">
            <template #header><b>碳足迹来源构成（扇形图）</b></template>
            <svg :viewBox="'0 0 320 280'" style="width:100%;height:300px">
              <path v-for="(p,i) in pieSlices" :key="'p'+i" :d="p.path" :fill="p.color" stroke="#fff" stroke-width="1.5" />
              <text v-for="(p,i) in pieSlices" :key="'pt'+i" :x="p.lx" :y="p.ly" text-anchor="middle" font-size="11" fill="#fff" font-weight="600">{{ p.percent }}%</text>
              <g :transform="'translate(8,196)'">
                <g v-for="(p,i) in pieSlices" :key="'pl'+i" :transform="'translate(0,'+(i*20)+')'">
                  <rect x="0" y="0" :width="12" :height="12" rx="2" :fill="p.color" />
                  <text x="18" y="10" font-size="12" fill="#606266">{{ p.label }}</text>
                </g>
              </g>
            </svg>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { batchApi, carbonApi } from '../api'

const TYPE_NAME = {
  FERTILIZE: '施肥', IRRIGATE: '灌溉', MACHINE: '农机作业', PESTICIDE: '农药施用',
  FILM: '农膜使用', HARVEST: '收获', PACKAGE: '包装', TRANSPORT: '运输'
}
const COLORS = ['#07c160', '#409eff', '#e6a23c', '#f56c6c', '#9b59b6', '#1abc9c', '#34495e', '#f39c12']
const W = 640, H = 280, padL = 46, padR = 16, padT = 22, padB = 34
const BW = 320, BH = 280, bpadL = 40, bpadR = 12, bpadT = 18, bpadB = 30

const loading = ref(true)
const err = ref('')
const debug = ref({ batches: 0, tried: 0, ok: 0, firstErr: '' })
const batches = ref([])
const carbons = ref([])   // { batchId, batchNo, total, unit, items:[{farmingType,emission}] }

const shortNo = (no) => (no || '').replace(/^AGRI\d*/, '') || no

async function load() {
  loading.value = true
  err.value = ''
  try {
    const list = await batchApi.list()
    batches.value = Array.isArray(list) ? list : []
    let tried = 0, ok = 0, firstErr = ''
    // 并行拉取每个批次的碳报告（单条失败不影响其余）
    const reports = await Promise.all(
      batches.value.map(b =>
        carbonApi.report(b.id)
          .then(r => { tried++; ok++; return { ...r, batchId: b.id, batchNo: b.batchNo } })
          .catch(e => { tried++; if (!firstErr) firstErr = (e && e.message) || 'request failed'; return null })
      )
    )
    debug.value = { batches: batches.value.length, tried, ok, firstErr }
    carbons.value = reports
      .filter(r => r && r.totalEmission != null)
      .map(r => ({
        batchId: r.batchId,
        batchNo: r.batchNo,
        total: Number(r.totalEmission),
        unit: Number(r.unitEmission),
        items: Array.isArray(r.items) ? r.items : []
      }))
      .sort((a, b) => a.batchId - b.batchId)
  } catch (e) {
    err.value = (e && e.message) || '加载失败'
  } finally {
    loading.value = false
  }
}

const stats = computed(() => {
  const n = carbons.value.length
  const total = carbons.value.reduce((s, c) => s + (c.total || 0), 0)
  const byType = {}
  carbons.value.forEach(c => c.items.forEach(it => {
    byType[it.farmingType] = (byType[it.farmingType] || 0) + Number(it.emission || 0)
  }))
  const topSrc = Object.entries(byType).sort((a, b) => b[1] - a[1])[0]
  return [
    { label: '批次总数', value: batches.value.length },
    { label: '已核算碳报告', value: n },
    { label: '总碳足迹(kgCO₂e)', value: Math.round(total).toLocaleString() },
    { label: '最大排放来源', value: topSrc ? (TYPE_NAME[topSrc[0]] || topSrc[0]) : '-' }
  ]
})

/* ---------- 折线图（各批次总排放） ---------- */
const lineMax = computed(() => {
  const m = Math.max(1, ...carbons.value.map(c => c.total))
  return Math.ceil(m / 2000) * 2000
})
const lineYTicks = computed(() => {
  const arr = []
  const step = lineMax.value / 4
  for (let i = 0; i <= 4; i++) {
    const v = step * i
    arr.push({ y: H - padB - (v / lineMax.value) * (H - padT - padB), label: Math.round(v) })
  }
  return arr
})
const lineDots = computed(() => {
  const n = carbons.value.length
  const innerW = W - padL - padR
  return carbons.value.map((c, i) => ({
    x: n === 1 ? padL + innerW / 2 : padL + (innerW * i) / (n - 1),
    y: H - padB - (c.total / lineMax.value) * (H - padT - padB),
    label: shortNo(c.batchNo)
  }))
})
const linePoints = computed(() => lineDots.value.map(d => `${d.x.toFixed(1)},${d.y.toFixed(1)}`).join(' '))

/* ---------- 柱状图（各批次单位碳足迹） ---------- */
const barMax = computed(() => {
  const m = Math.max(0.001, ...carbons.value.map(c => c.unit))
  return Math.ceil(m * 10) / 10
})
const barYTicks = computed(() => {
  const arr = []
  const step = barMax.value / 4
  for (let i = 0; i <= 4; i++) {
    const v = step * i
    arr.push({ y: BH - bpadB - (v / barMax.value) * (BH - bpadT - bpadB), label: v.toFixed(2) })
  }
  return arr
})
const barRects = computed(() => {
  const n = carbons.value.length
  const innerW = BW - bpadL - bpadR
  const slot = innerW / Math.max(n, 1)
  const bw = Math.min(18, slot * 0.6)
  return carbons.value.map((c, i) => {
    const h = (c.unit / barMax.value) * (BH - bpadT - bpadB)
    return {
      x: bpadL + slot * i + (slot - bw) / 2,
      y: BH - bpadB - h,
      w: bw,
      h,
      label: shortNo(c.batchNo),
      valText: c.unit.toFixed(3)
    }
  })
})

/* ---------- 扇形图（来源构成占比） ---------- */
const pieSlices = computed(() => {
  const byType = {}
  carbons.value.forEach(c => c.items.forEach(it => {
    byType[it.farmingType] = (byType[it.farmingType] || 0) + Number(it.emission || 0)
  }))
  const entries = Object.entries(byType).sort((a, b) => b[1] - a[1])
  const total = entries.reduce((s, e) => s + e[1], 0) || 1
  const cx = 150, cy = 130, r = 100
  let acc = 0
  return entries.map((e, i) => {
    const frac = e[1] / total
    const a0 = acc * 2 * Math.PI - Math.PI / 2
    acc += frac
    const a1 = acc * 2 * Math.PI - Math.PI / 2
    const large = frac > 0.5 ? 1 : 0
    const x0 = cx + r * Math.cos(a0), y0 = cy + r * Math.sin(a0)
    const x1 = cx + r * Math.cos(a1), y1 = cy + r * Math.sin(a1)
    const path = `M ${cx} ${cy} L ${x0.toFixed(2)} ${y0.toFixed(2)} A ${r} ${r} 0 ${large} 1 ${x1.toFixed(2)} ${y1.toFixed(2)} Z`
    const mid = (a0 + a1) / 2
    const lr = r * 0.62
    return {
      path, color: COLORS[i % COLORS.length],
      label: TYPE_NAME[e[0]] || e[0],
      percent: (frac * 100).toFixed(1),
      lx: cx + lr * Math.cos(mid), ly: cy + lr * Math.sin(mid)
    }
  })
})

onMounted(load)
</script>
