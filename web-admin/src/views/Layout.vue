<template>
  <el-container style="height:100vh">
    <el-aside width="200px" style="background:#001529">
      <div style="color:#fff;text-align:center;padding:16px 0;font-weight:600">溯源 · 碳足迹平台</div>
      <el-menu background-color="#001529" text-color="#fff" active-text-color="#409EFF" router>
        <el-menu-item index="/home">数据概览</el-menu-item>
        <el-menu-item index="/batch">批次管理</el-menu-item>
        <el-menu-item index="/farmers">农户管理</el-menu-item>
        <el-menu-item index="/carbon">碳报告审核</el-menu-item>
        <el-menu-item index="/analysis">数据分析</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;display:flex;align-items:center;justify-content:flex-end;border-bottom:1px solid #eee">
        <span style="margin-right:16px">{{ user.realName || user.username }}</span>
        <el-button text @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('trace_user') || '{}') } catch { return {} }
})

function logout() {
  localStorage.removeItem('trace_token')
  localStorage.removeItem('trace_user')
  router.push('/login')
}
</script>
