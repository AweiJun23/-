<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 style="text-align:center;margin-bottom:8px">收购商后台登录</h2>
      <p style="text-align:center;color:#909399;margin-top:0">城乡农品轻量溯源与碳足迹存证系统</p>
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="如 buyer01" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="默认 123456" />
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="onLogin">登录</el-button>
      </el-form>
      <p style="text-align:center;color:#c0c4cc;font-size:12px;margin-bottom:0">
        演示账号：buyer01 / admin　密码：123456
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const form = reactive({ username: 'buyer01', password: '123456' })
const loading = ref(false)

async function onLogin() {
  loading.value = true
  try {
    const data = await authApi.login(form)
    localStorage.setItem('trace_token', data.token)
    localStorage.setItem('trace_user', JSON.stringify(data))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error((e && e.message) || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: url('/login-bg.png') no-repeat center center / cover;
}
/* 蓝色半透明遮罩，提升登录卡可读性 */
.login-wrap::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(160deg, rgba(6,32,74,0.45), rgba(3,20,48,0.25));
}
.login-card {
  width: 380px;
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(6px);
  border: none;
  border-radius: 12px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.28);
}
</style>
