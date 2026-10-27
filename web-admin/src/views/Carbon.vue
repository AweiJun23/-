<template>
  <div>
    <h2>碳报告审核</h2>
    <el-input v-model="batchNo" placeholder="输入批次编号查询碳足迹报告" style="max-width:360px;margin-right:8px" />
    <el-button type="primary" :loading="loading" @click="query">查询</el-button>

    <el-card v-if="report" style="margin-top:16px">
      <template #header>
        碳足迹报告
        <el-tag v-if="report.reportHash" type="success" style="margin-left:8px">已存证</el-tag>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="总排放(kgCO2e)">{{ report.totalEmission }}</el-descriptions-item>
        <el-descriptions-item label="单位排放(kgCO2e/kg)">{{ report.unitEmission }}</el-descriptions-item>
        <el-descriptions-item label="报告哈希">{{ report.reportHash }}</el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ report.reportTime }}</el-descriptions-item>
      </el-descriptions>
      <p style="color:#67c23a;margin-top:8px">AI 低碳建议：{{ report.aiAdvice }}</p>

      <p style="font-weight:600;margin:12px 0 4px">分项明细</p>
      <el-table :data="report.items || []">
        <el-table-column prop="farmingType" label="环节" />
        <el-table-column prop="materialName" label="物料" />
        <el-table-column prop="amount" label="用量" />
        <el-table-column prop="emission" label="排放(kgCO2e)" />
        <el-table-column prop="proportion" label="占比(%)" />
      </el-table>
    </el-card>
    <el-empty v-else-if="searched" description="未查询到报告，请确认批次编号" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { carbonApi } from '../api'

const batchNo = ref('')
const report = ref(null)
const loading = ref(false)
const searched = ref(false)

async function query() {
  if (!batchNo.value) return
  loading.value = true
  searched.value = true
  try {
    report.value = await carbonApi.reportByNo(batchNo.value) || null
  } catch (e) {
    report.value = null
    ElMessage.error((e && e.message) || '查询失败')
  } finally {
    loading.value = false
  }
}
</script>
