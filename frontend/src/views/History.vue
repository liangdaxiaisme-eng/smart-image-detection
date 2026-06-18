<template>
  <div class="layout">
    <el-menu mode="horizontal" :ellipsis="false" class="top-nav">
      <el-menu-item><b style="font-size:18px;color:#2a9d8f">🔍 智能检测系统</b></el-menu-item>
      <el-menu-item @click="$router.push('/dashboard')">首页</el-menu-item>
      <el-menu-item @click="$router.push('/upload')">上传检测</el-menu-item>
      <el-menu-item @click="$router.push('/history')">检测历史</el-menu-item>
      <el-menu-item @click="$router.push('/stats')">数据统计</el-menu-item>
      <div class="flex-grow" />
      <el-menu-item @click="logout">退出</el-menu-item>
    </el-menu>
    <div class="main-content">
      <el-card><template #header><span style="font-size:18px;font-weight:bold">检测历史记录</span></template>
        <el-table :data="results" border stripe v-loading="loading" style="width:100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="originalName" label="文件名" min-width="150" />
          <el-table-column label="识别统计" width="250">
            <template #default="{row}">
              <el-tag v-for="c in row.statistics?.per_class||[]" :key="c.class_name" size="small" style="margin:2px">{{ c.class_name }}:{{ c.count }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="检测时间" width="180" />
          <el-table-column label="操作" width="180">
            <template #default="{row}">
              <el-button size="small" type="primary" @click="showDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      <el-dialog v-model="detailVisible" title="检测详情" width="700px">
        <img v-if="detail.resultImageUrl" :src="detail.resultImageUrl" style="width:100%;border-radius:6px" />
        <p>文件名：{{ detail.originalName }}</p>
        <p>检测时间：{{ detail.createTime }}</p>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'

const router = useRouter()
const results = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref({})

const logout = () => { localStorage.clear(); router.push('/login') }
const showDetail = (row) => { detail.value = row; detailVisible.value = true }

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get('/detect/results')
    if (res.code === 200) results.value = res.data
  } catch {}
  finally { loading.value = false }
})
</script>

<style scoped>
.layout { background:#f5f7fa; min-height:100vh; }
.top-nav { padding:0 20px; }
.flex-grow { flex-grow:1; }
.main-content { padding:20px; max-width:1200px; margin:0 auto; }
</style>
