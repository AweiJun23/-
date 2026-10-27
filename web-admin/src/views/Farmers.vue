<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h2>农户管理</h2>
      <el-button type="primary" @click="openAdd">添加农户</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="channel" label="常用渠道" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="添加农户" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="form.realName" placeholder="农户真实姓名" /></el-form-item>
        <el-form-item label="账号"><el-input v-model="form.username" placeholder="登录账号，如 farmer10" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" placeholder="登录密码，默认 123456" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="选填" /></el-form-item>
        <el-form-item label="渠道">
          <el-select v-model="form.channel" style="width:100%">
            <el-option label="APP" value="APP" />
            <el-option label="MINIAPP" value="MINIAPP" />
            <el-option label="WEB" value="WEB" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onAdd">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authApi, farmerApi } from '../api'

const rows = ref([])
const loading = ref(false)
const dialog = ref(false)
const submitting = ref(false)

const form = reactive({
  realName: '', username: '', password: '123456', phone: '', channel: 'APP'
})

async function load() {
  loading.value = true
  try {
    const me = JSON.parse(localStorage.getItem('trace_user') || '{}')
    rows.value = await farmerApi.list({ coopId: me.coopId }) || []
  } catch (e) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function openAdd() {
  form.realName = ''
  form.username = ''
  form.password = '123456'
  form.phone = ''
  form.channel = 'APP'
  dialog.value = true
}

async function onAdd() {
  if (!form.username.trim()) { ElMessage.error('请输入账号'); return }
  if (!form.password) { ElMessage.error('请输入密码'); return }
  submitting.value = true
  try {
    const me = JSON.parse(localStorage.getItem('trace_user') || '{}')
    await authApi.register({
      username: form.username.trim(),
      password: form.password,
      role: 'FARMER',
      realName: form.realName,
      phone: form.phone,
      coopId: me.coopId || 1,
      channel: form.channel
    })
    ElMessage.success('农户添加成功')
    dialog.value = false
    load()
  } catch (e) {
    ElMessage.error((e && e.message) || '添加失败')
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除农户「${row.realName || row.username}」吗？该账号将无法登录。`, '删除确认', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await authApi.deleteFarmer(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error((e && e.message) || '删除失败')
  }
}

onMounted(load)
</script>
