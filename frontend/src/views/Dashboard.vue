<template>
  <div class="layout">
    <el-menu mode="horizontal" :ellipsis="false" class="top-nav">
      <el-menu-item><b style="font-size:18px;color:#2a9d8f">🔍 智能检测系统</b></el-menu-item>
      <el-menu-item index="1" @click="$router.push('/dashboard')">首页</el-menu-item>
      <el-menu-item index="2" @click="$router.push('/upload')">上传检测</el-menu-item>
      <el-menu-item index="3" @click="$router.push('/history')">检测历史</el-menu-item>
      <el-menu-item index="4" @click="$router.push('/stats')">数据统计</el-menu-item>
      <div class="flex-grow" />
      <el-menu-item><span>{{ user?.nickname || user?.username }}</span></el-menu-item>
      <el-menu-item @click="logout">退出</el-menu-item>
    </el-menu>
    <div class="main-content">
      <el-row :gutter="20" style="margin-bottom:20px">
        <el-col :span="6"><el-card shadow="hover"><h3>{{ stats.totalDetections || 0 }}</h3><p>总检测次数</p></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><h3>{{ stats.todayDetections || 0 }}</h3><p>今日检测</p></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><h3>{{ stats.totalObjects || 0 }}</h3><p>识别目标总数</p></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><h3>{{ stats.totalImages || 0 }}</h3><p>上传图片数</p></el-card></el-col>
      </el-row>
      <el-card><template #header><span>最近检测记录</span></template>
        <el-table :data="recentResults" v-if="recentResults.length">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="originalName" label="文件名" />
          <el-table-column label="识别目标" width="200">
            <template #default="{row}">
              <el-tag v-for="c in row.statistics?.per_class||[]" :key="c.class_name" size="small" style="margin:2px">{{ c.class_name }}:{{ c.count }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="检测时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="{row}"><el-button size="small" @click="$router.push('/upload')">查看</el-button></template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无检测记录" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = ref({})
const recentResults = ref([])

const logout = () => { localStorage.clear(); router.push('/login') }

onMounted(async () => {
  try {
    const [s, r] = await Promise.all([
      api.get('/stats/overview'),
      api.get('/detect/results', { params: { limit: 5 } })
    ])
    if (s.code === 200) stats.value = s.data
    if (r.code === 200) recentResults.value = r.data
  } catch {}
})
</script>

<style scoped>
.layout { background:#f5f7fa; min-height:100vh; }
.top-nav { padding:0 20px; }
.flex-grow { flex-grow:1; }
.main-content { padding:20px; max-width:1200px; margin:0 auto; }
.el-card h3 { font-size:28px; color:#264653; margin:0; }
.el-card p { color:#999; margin:5px 0 0; font-size:14px; }
</style>
