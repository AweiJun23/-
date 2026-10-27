<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h2>批次管理</h2>
      <el-button type="primary" @click="dialog = true">新建批次</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="batchNo" label="批次编号" />
      <el-table-column prop="productName" label="产品" />
      <el-table-column prop="originProvince" label="产地" />
      <el-table-column prop="areaMu" label="面积(亩)" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.batchNo)">溯源/核验</el-button>
          <el-button link type="success" @click="openQr(row.batchNo)">二维码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="新建批次" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="产品名称"><el-input v-model="form.productName" /></el-form-item>
        <el-form-item label="所属农户">
          <el-select v-model="form.farmerId" placeholder="请选择农户" style="width:100%">
            <el-option v-for="f in farmers" :key="f.id" :label="(f.realName||f.username)+' ('+f.username+')'" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产地省"><el-input v-model="form.originProvince" /></el-form-item>
        <el-form-item label="产地市"><el-input v-model="form.originCity" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.originDetail" /></el-form-item>
        <el-form-item label="种植面积"><el-input v-model="form.areaMu" type="number" /></el-form-item>
        <el-form-item label="预估产量"><el-input v-model="form.expectedYield" type="number" /></el-form-item>
        <el-form-item label="种植开始"><el-date-picker v-model="form.plantStartDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="种植结束"><el-date-picker v-model="form.plantEndDate" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="qrVisible" title="溯源二维码" width="360px">
      <div style="text-align:center" v-loading="qrLoading">
        <img v-if="qrUrl" :src="qrUrl" style="width:240px;height:240px" />
        <p style="color:#909399">扫码进入消费者 H5 验真页面</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { batchApi, farmerApi } from '../api'

const router = useRouter()
const rows = ref([])
const farmers = ref([])
const loading = ref(false)
const dialog = ref(false)
const submitting = ref(false)
const qrVisible = ref(false)
const qrUrl = ref('')
const qrLoading = ref(false)

const form = reactive({
  farmerId: null, coopId: null,
  productName: '', originProvince: '', originCity: '', originDetail: '',
  areaMu: null, expectedYield: null, plantStartDate: '', plantEndDate: ''
})

async function load() {
  loading.value = true
  try {
    // 收购商只看本合作社批次（后端按 coopId 过滤），这里直接取列表
    const me = JSON.parse(localStorage.getItem('trace_user') || '{}')
    rows.value = await batchApi.list({ coopId: me.coopId }) || []
    // 拉取农户列表，供创建时选择
    try {
      farmers.value = await farmerApi.list() || []
      if (!form.farmerId && farmers.value.length) form.farmerId = farmers.value[0].id
    } catch (e) { farmers.value = [] }
  } catch (e) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function onCreate() {
  submitting.value = true
  try {
    const me = JSON.parse(localStorage.getItem('trace_user') || '{}')
    if (!form.farmerId) {
      ElMessage.error('请选择所属农户')
      return
    }
    form.coopId = me.coopId || 1
    await batchApi.create({ ...form })
    ElMessage.success('批次创建成功')
    dialog.value = false
    load()
  } catch (e) {
    ElMessage.error((e && e.message) || '创建失败')
  } finally {
    submitting.value = false
  }
}

function goDetail(batchNo) {
  router.push(`/batch/${batchNo}`)
}

async function openQr(batchNo) {
  qrVisible.value = true
  qrLoading.value = true
  qrUrl.value = ''
  try {
    const dataUrl = await batchApi.qrBase64(batchNo)
    if (!dataUrl || dataUrl.indexOf('data:image') !== 0) {
      throw new Error('二维码生成失败')
    }
    qrUrl.value = dataUrl
  } catch (e) {
    ElMessage.error((e && e.message) || '二维码加载失败')
    qrVisible.value = false
  } finally {
    qrLoading.value = false
  }
}

onMounted(load)
</script>
