<template>
  <div>
    <h2>数据概览</h2>
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.title">
        <el-card shadow="hover">
          <div style="color:#909399;font-size:13px">{{ c.title }}</div>
          <div style="font-size:26px;font-weight:600;margin-top:6px">{{ c.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:16px">
      <template #header>平台说明</template>
      <p>本平台面向<strong>收购商</strong>，提供农产品批次管理、农户管理、碳足迹报告审核能力。</p>
      <ul>
        <li>批次管理：创建批次、查看溯源时间线、下载溯源二维码。</li>
        <li>农户管理：查看管辖合作社下的农户列表与基本信息。</li>
        <li>碳报告审核：查看各批次全流程碳排放与 AI 低碳建议，核验链上存证。</li>
      </ul>
      <p style="color:#909399">消费者可通过扫描批次二维码，在 H5 页面查看完整农事记录、碳标签并验真。</p>
    </el-card>
  </div>
</template>

<script setup>
const cards = [
  { title: '在管批次', value: '—' },
  { title: '管辖农户', value: '—' },
  { title: '已出碳报告', value: '—' },
  { title: '链上验真', value: 'SHA256' }
]
</script>
