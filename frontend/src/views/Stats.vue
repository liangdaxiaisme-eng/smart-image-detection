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
      <el-row :gutter="20" style="margin-bottom:20px">
        <el-col :span="8"><el-card shadow="hover"><h3>{{ overview.totalDetections || 0 }}</h3><p>累计检测次数</p></el-card></el-col>
        <el-col :span="8"><el-card shadow="hover"><h3>{{ overview.totalObjects || 0 }}</h3><p>累计识别目标数</p></el-card></el-col>
        <el-col :span="8"><el-card shadow="hover"><h3>{{ overview.todayDetections || 0 }}</h3><p>今日检测</p></el-card></el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card><template #header>类别分布</template>
            <div ref="pieRef" style="height:350px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card><template #header>每日检测趋势</template>
            <div ref="lineRef" style="height:350px"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import api from '@/utils/api'

const router = useRouter()
const overview = ref({})
const pieRef = ref(null)
const lineRef = ref(null)

const logout = () => { localStorage.clear(); router.push('/login') }

const renderPie = (data) => {
  if (!pieRef.value || !data?.length) return
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['30%', '60%'],
      data: data.map(d => ({ name: d.class_name, value: d.count })),
      label: { show: true, formatter: '{b}: {c}' },
    }]
  })
}

const renderLine = (data) => {
  if (!lineRef.value || !data?.length) return
  const chart = echarts.init(lineRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', data: data.map(d => d.count), smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#2a9d8f' } }]
  })
}

onMounted(async () => {
  try {
    const [o, c, d] = await Promise.all([
      api.get('/stats/overview'),
      api.get('/stats/category'),
      api.get('/stats/daily')
    ])
    if (o.code === 200) overview.value = o.data
    nextTick(() => {
      if (c.code === 200) renderPie(c.data)
      if (d.code === 200) renderLine(d.data)
    })
  } catch {}
})
</script>

<style scoped>
.layout { background:#f5f7fa; min-height:100vh; }
.top-nav { padding:0 20px; }
.flex-grow { flex-grow:1; }
.main-content { padding:20px; max-width:1200px; margin:0 auto; }
.el-card h3 { font-size:28px; color:#264653; margin:0; }
.el-card p { color:#999; margin:5px 0 0; }
</style>
