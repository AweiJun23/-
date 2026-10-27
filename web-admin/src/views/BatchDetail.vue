<template>
  <div v-loading="loading">
    <el-page-header @back="$router.push('/batch')" content="批次溯源 / 核验" />

    <el-card style="margin-top:16px">
      <template #header>{{ trace.productName || batchNo }} · 基本信息</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="批次编号">{{ trace.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="产地">{{ trace.originProvince }}{{ trace.originCity }}{{ trace.originDetail }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ trace.status }}</el-descriptions-item>
        <el-descriptions-item label="种植面积(亩)">{{ trace.areaMu }}</el-descriptions-item>
        <el-descriptions-item label="实际产量(kg)">{{ trace.actualYield }}</el-descriptions-item>
        <el-descriptions-item label="批次验真">
          <el-tag :type="trace.batchVerified ? 'success' : 'danger'">
            {{ trace.batchVerified ? '已上链验真' : '未验真' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header>农事记录时间线</template>
      <el-table :data="trace.farmingList || []">
        <el-table-column prop="farmingTypeDesc" label="类型" />
        <el-table-column prop="materialName" label="物料" />
        <el-table-column prop="amount" label="用量" />
        <el-table-column prop="unit" label="单位" />
        <el-table-column prop="workDate" label="时间" />
        <el-table-column prop="checkStatus" label="AI校验">
          <template #default="{ row }">
            <el-tag :type="row.checkStatus === 'ABNORMAL' ? 'warning' : 'success'">{{ row.checkStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiAdvice" label="AI建议" />
      </el-table>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header>
        碳足迹报告
        <el-tag v-if="trace.carbonReport" :type="trace.carbonVerified ? 'success' : 'danger'" style="margin-left:8px">
          {{ trace.carbonVerified ? '已存证' : '未存证' }}
        </el-tag>
      </template>
      <template v-if="trace.carbonReport">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总排放(kgCO2e)">{{ trace.carbonReport.totalEmission }}</el-descriptions-item>
          <el-descriptions-item label="单位排放(kgCO2e/kg)">{{ trace.carbonReport.unitEmission }}</el-descriptions-item>
        </el-descriptions>
        <p style="margin:12px 0 4px;font-weight:600">分项明细</p>
        <el-table :data="trace.carbonReport.items || []">
          <el-table-column prop="farmingType" label="环节" />
          <el-table-column prop="materialName" label="物料" />
          <el-table-column prop="amount" label="用量" />
          <el-table-column prop="emission" label="排放(kgCO2e)" />
          <el-table-column prop="proportion" label="占比(%)" />
        </el-table>
        <p style="color:#67c23a;margin-top:8px">AI 低碳建议：{{ trace.carbonReport.aiAdvice }}</p>
        <el-button type="primary" plain @click="reVerify">重新链上验真</el-button>
      </template>
      <el-empty v-else description="暂无碳足迹报告" />
    </el-card>

    <el-card style="margin-top:16px">
      <template #header>
        绿色补贴申报
        <el-tag v-if="subsidy.reportNo" type="success" style="margin-left:8px">{{ subsidy.status || 'GENERATED' }}</el-tag>
      </template>
      <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center">
        <el-button type="primary" :loading="genLoading" @click="onGenerate">生成补贴申报</el-button>
        <el-button :disabled="!subsidy.contentMd" @click="viewVisible = true">查看</el-button>
        <el-button type="success" plain :disabled="!subsidy.contentMd" @click="onDownload">下载</el-button>
      </div>
      <p v-if="subsidy.reportNo" style="color:#909399;margin:10px 0 0;font-size:13px">
        申报单号：{{ subsidy.reportNo }}　·　生成时间：{{ subsidy.generatedTime }}
      </p>
      <el-empty v-if="!subsidy.reportNo && !genLoading" description="尚未生成申报材料，点击「生成补贴申报」自动整理农事台账与碳报告" />
    </el-card>

    <el-dialog v-model="viewVisible" title="补贴申报材料（Markdown）" width="680px" top="6vh">
      <pre class="md-preview">{{ subsidy.contentMd }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { batchApi, chainApi, reportApi } from '../api'

const route = useRoute()
const batchNo = route.params.batchNo
const trace = ref({})
const loading = ref(false)
const subsidy = ref({})
const genLoading = ref(false)
const viewVisible = ref(false)

async function load() {
  loading.value = true
  try {
    trace.value = await batchApi.trace(batchNo) || {}
    await loadSubsidy()
  } catch (e) {
    ElMessage.error((e && e.message) || '加载失败')
  } finally {
    loading.value = false
  }
}

// 加载该批次最新的补贴申报材料（无则保持空）
async function loadSubsidy() {
  if (!trace.value.id) return
  try {
    subsidy.value = await reportApi.latest(trace.value.id) || {}
  } catch (e) {
    subsidy.value = {}
  }
}

// 生成补贴申报：调 trace-report 编排（取批次+碳报告 → 智能体生成 → 落库）
async function onGenerate() {
  if (!trace.value.id) { ElMessage.error('批次信息未加载'); return }
  genLoading.value = true
  try {
    await reportApi.generate(trace.value.id)
    ElMessage.success('申报材料已生成')
    await loadSubsidy()
  } catch (e) {
    ElMessage.error((e && e.message) || '生成失败')
  } finally {
    genLoading.value = false
  }
}

// 下载：前端取 contentMd 生成 .md 文件（避免下载接口需鉴权、<a> 无法带 token 的问题）
function onDownload() {
  if (!subsidy.value.contentMd) return
  const blob = new Blob([subsidy.value.contentMd], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = (subsidy.value.reportNo || ('subsidy_' + batchNo)) + '.md'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

async function reVerify() {
  const r = trace.value.carbonReport
  if (!r) return
  try {
    const res = await chainApi.verify({
      bizType: 'CARBON_REPORT',
      bizId: String(r.reportId),
      dataHash: r.reportHash
    })
    ElMessage.success(res && res.verified ? '验真通过，数据未被篡改' : '验真未通过：' + (res && res.reason))
    load()
  } catch (e) {
    ElMessage.error((e && e.message) || '验真失败')
  }
}

onMounted(load)
</script>

<style scoped>
.md-preview {
  white-space: pre-wrap;
  word-break: break-word;
  background: #f6f8fa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
  margin: 0;
  max-height: 60vh;
  overflow: auto;
  font-size: 13px;
  line-height: 1.7;
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
}
</style>
