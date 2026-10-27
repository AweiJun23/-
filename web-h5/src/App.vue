<template>
  <div class="h5">
    <header class="top">农产品溯源 · 碳标签验真</header>

    <section class="search">
      <input v-model="batchNo" placeholder="请输入或扫描批次编号" />
      <button @click="load">查询</button>
    </section>

    <div v-if="loading" class="tip">加载中…</div>
    <div v-else-if="err" class="tip err">{{ err }}</div>

    <template v-else-if="data">
      <!-- 产品信息 -->
      <section class="card">
        <h3>{{ data.productName }}</h3>
        <p class="sub">批次 {{ data.batchNo }}</p>
        <div class="kv"><span>产地</span><b>{{ data.originProvince }}{{ data.originCity }}{{ data.originDetail }}</b></div>
        <div class="kv"><span>种植面积</span><b>{{ data.areaMu }} 亩</b></div>
        <div class="kv"><span>实际产量</span><b>{{ data.actualYield }} kg</b></div>
      </section>

      <!-- 存证验真状态 -->
      <section class="card verify">
        <div class="vitem">
          <span>批次数据</span>
          <b :class="data.batchVerified ? 'ok' : 'no'">{{ data.batchVerified ? '链上验真通过' : '未验真' }}</b>
        </div>
        <div class="vitem">
          <span>碳足迹报告</span>
          <b :class="data.carbonVerified ? 'ok' : 'no'">{{ data.carbonVerified ? '已存证' : '未存证' }}</b>
        </div>
        <button class="verify-btn" :disabled="verifying" @click="reVerify">重新链上验真</button>
      </section>

      <!-- 碳标签 -->
      <section class="card" v-if="data.carbonReport">
        <h4>碳足迹标签</h4>
        <div class="carbon">
          <div><span>总排放</span><b>{{ data.carbonReport.totalEmission }}</b><i>kgCO2e</i></div>
          <div><span>单位排放</span><b>{{ data.carbonReport.unitEmission }}</b><i>kgCO2e/kg</i></div>
        </div>
        <p class="advice">{{ data.carbonReport.aiAdvice }}</p>
        <table v-if="data.carbonReport.items && data.carbonReport.items.length">
          <thead><tr><th>环节</th><th>物料</th><th>排放</th><th>占比</th></tr></thead>
          <tbody>
            <tr v-for="(it, i) in data.carbonReport.items" :key="i">
              <td>{{ it.farmingType }}</td><td>{{ it.materialName }}</td>
              <td>{{ it.emission }}</td><td>{{ it.proportion }}%</td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 农事时间线 -->
      <section class="card">
        <h4>农事记录时间线</h4>
        <ul class="timeline">
          <li v-for="(f, i) in (data.farmingList || [])" :key="i">
            <div class="t-type">{{ f.farmingTypeDesc || f.farmingType }}</div>
            <div class="t-meta">{{ f.workDate }} · {{ f.materialName }} {{ f.amount }}{{ f.unit }}</div>
            <div class="t-check" :class="f.checkStatus === 'ABNORMAL' ? 'warn' : ''">
              AI校验：{{ f.checkStatus }}<span v-if="f.aiAdvice">｜{{ f.aiAdvice }}</span>
            </div>
          </li>
        </ul>
        <p v-if="!data.farmingList || !data.farmingList.length" class="tip">暂无农事记录</p>
      </section>

      <p class="footer">数据由联盟链轻节点哈希存证，扫码即可核验是否被篡改。</p>
    </template>

    <div v-else class="tip">请输入批次编号查询农产品溯源与碳足迹信息。</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { batchApi, chainApi } from './api'

const batchNo = ref('')
const data = ref(null)
const loading = ref(false)
const verifying = ref(false)
const err = ref('')

function parseQuery() {
  const p = new URLSearchParams(location.search)
  const q = p.get('batchNo') || p.get('no')
  if (q) { batchNo.value = q; load() }
}

async function load() {
  if (!batchNo.value) return
  loading.value = true
  err.value = ''
  try {
    data.value = await batchApi.trace(batchNo.value)
  } catch (e) {
    data.value = null
    err.value = (e && e.message) || '查询失败，请检查批次编号'
  } finally {
    loading.value = false
  }
}

async function reVerify() {
  const r = data.value && data.value.carbonReport
  if (!r) return
  verifying.value = true
  try {
    const res = await chainApi.verify({
      bizType: 'CARBON_REPORT',
      bizId: String(r.reportId),
      dataHash: r.reportHash
    })
    alert(res && res.verified ? '验真通过，数据未被篡改 ✅' : '验真未通过：' + (res && res.reason || '未知'))
    load()
  } catch (e) {
    alert('验真失败：' + ((e && e.message) || ''))
  } finally {
    verifying.value = false
  }
}

onMounted(parseQuery)
</script>

<style>
* { box-sizing: border-box; }
body { margin: 0; background: #eef2f5; font-family: -apple-system, 'PingFang SC', sans-serif; color: #303133; }
.h5 { max-width: 480px; margin: 0 auto; min-height: 100vh; padding-bottom: 24px; }
.top { background: #07c160; color: #fff; text-align: center; padding: 14px; font-size: 17px; font-weight: 600; }
.search { display: flex; gap: 8px; padding: 12px; }
.search input { flex: 1; padding: 10px; border: 1px solid #dcdfe6; border-radius: 8px; font-size: 15px; }
.search button { padding: 0 18px; border: none; background: #07c160; color: #fff; border-radius: 8px; font-size: 15px; }
.card { background: #fff; margin: 10px 12px; border-radius: 12px; padding: 14px; box-shadow: 0 1px 4px rgba(0,0,0,.05); }
.card h3 { margin: 0; font-size: 18px; }
.card h4 { margin: 0 0 10px; font-size: 15px; color: #07c160; }
.sub { color: #909399; font-size: 12px; margin: 4px 0 10px; }
.kv { display: flex; justify-content: space-between; padding: 4px 0; font-size: 14px; }
.kv span { color: #909399; }
.verify .vitem { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.ok { color: #07c160; }
.no { color: #f56c6c; }
.verify-btn { width: 100%; margin-top: 8px; padding: 10px; border: none; background: #07c160; color: #fff; border-radius: 8px; font-size: 15px; }
.verify-btn:disabled { opacity: .6; }
.carbon { display: flex; gap: 12px; }
.carbon > div { flex: 1; background: #f6fbf8; border-radius: 8px; padding: 10px; text-align: center; }
.carbon span { display: block; color: #909399; font-size: 12px; }
.carbon b { font-size: 20px; color: #07c160; }
.carbon i { font-style: normal; font-size: 11px; color: #c0c4cc; }
.advice { font-size: 13px; color: #606266; background: #f6fbf8; padding: 8px; border-radius: 8px; margin: 10px 0; }
table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 8px; }
th, td { border-bottom: 1px solid #eee; padding: 6px 4px; text-align: center; }
.timeline { list-style: none; margin: 0; padding: 0; }
.timeline li { border-left: 2px solid #07c160; padding: 6px 0 6px 12px; margin-left: 4px; }
.t-type { font-weight: 600; font-size: 14px; }
.t-meta { color: #909399; font-size: 12px; margin: 2px 0; }
.t-check { font-size: 12px; color: #606266; }
.t-check.warn { color: #e6a23c; }
.tip { text-align: center; color: #909399; padding: 24px 12px; font-size: 14px; }
.tip.err { color: #f56c6c; }
.footer { text-align: center; color: #c0c4cc; font-size: 11px; padding: 12px; }
</style>
