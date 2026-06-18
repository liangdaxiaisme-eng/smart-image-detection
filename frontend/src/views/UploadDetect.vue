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
      <el-row :gutter="20">
        <el-col :span="10">
          <el-card><template #header>上传图片</template>
            <div class="upload-area" @dragover.prevent @drop.prevent="handleDrop">
              <input ref="fileInput" type="file" accept="image/*" hidden @change="handleFileSelect" />
              <div v-if="!uploading && !previewUrl" class="upload-placeholder" @click="$refs.fileInput.click()">
                <el-icon style="font-size:40px;color:#ccc"><Plus /></el-icon>
                <p>拖拽或点击上传图片</p>
              </div>
              <div v-if="uploading" class="upload-progress">
                <el-progress type="circle" :percentage="uploadProgress" :width="120" />
                <p style="margin-top:10px;color:#999">上传中...</p>
              </div>
              <div v-if="previewUrl && !uploading" class="upload-preview" @click="$refs.fileInput.click()">
                <img :src="previewUrl" style="width:100%;max-height:300px;object-fit:contain;border-radius:6px" />
                <div class="upload-overlay">点击重新上传</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="14">
          <el-card><template #header>检测参数</template>
            <div style="display:flex;align-items:center;gap:15px">
              <span>置信度阈值：</span>
              <el-slider v-model="threshold" :min="0.1" :max="0.9" :step="0.05" show-input style="width:200px" />
              <el-button type="primary" size="large" @click="startDetect" :disabled="!imageId" :loading="detecting">开始检测</el-button>
            </div>
            <el-divider />
            <div v-if="resultImageUrl">
              <h3>检测结果</h3>
              <img :src="resultImageUrl" style="width:100%;max-height:350px;object-fit:contain;border:1px solid #ddd;border-radius:6px" />
              <h4 style="margin-top:15px">识别列表</h4>
              <el-table :data="detections" size="small" border>
                <el-table-column prop="class_name" label="类别" />
                <el-table-column prop="confidence" label="置信度" width="100">
                  <template #default="{row}">{{ (row.confidence * 100).toFixed(1) }}%</template>
                </el-table-column>
                <el-table-column label="位置" width="200">
                  <template #default="{row}">[{{ row.bbox.map(v=>v.toFixed(0)).join(', ') }}]</template>
                </el-table-column>
              </el-table>
              <div style="margin-top:10px">
                <el-tag v-for="s in statistics?.per_class||[]" :key="s.class_name" type="success" size="large" style="margin:3px">
                  {{ s.class_name }} × {{ s.count }}
                </el-tag>
              </div>
            </div>
            <el-empty v-else-if="!detecting" description="上传图片后点击「开始检测」" />
            <el-empty v-else description="检测中..." />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios'
import api from '@/utils/api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const token = localStorage.getItem('token')

const fileInput = ref(null)
const previewUrl = ref('')
const imageId = ref(null)
const threshold = ref(0.25)
const detecting = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const resultImageUrl = ref('')
const detections = ref([])
const statistics = ref(null)

const logout = () => { localStorage.clear(); router.push('/login') }

const doUpload = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
  formData.append('userId', userInfo.id || '')

  uploading.value = true
  uploadProgress.value = 0
  previewUrl.value = ''
  imageId.value = null

  try {
    const res = await axios.post('/api/image/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': token
      },
      onUploadProgress: (e) => {
        uploadProgress.value = Math.round((e.loaded / e.total) * 100)
      }
    })
    const data = res.data
    if (data.code === 200 && data.data) {
      imageId.value = data.data.imageId
      previewUrl.value = data.data.url
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(data.msg || '上传失败')
    }
  } catch (err) {
    ElMessage.error('上传失败：' + (err.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const handleFileSelect = (e) => {
  const file = e.target.files[0]
  if (file) doUpload(file)
}

const handleDrop = (e) => {
  const file = e.dataTransfer.files[0]
  if (file) doUpload(file)
}

const startDetect = async () => {
  if (!imageId.value) return ElMessage.warning('请先上传图片')
  detecting.value = true
  resultImageUrl.value = ''
  detections.value = []
  statistics.value = null
  try {
    const res = await api.post(`/detect/${imageId.value}`, null, { params: { threshold: threshold.value } })
    if (res.code === 200) {
      detections.value = res.data.detections || []
      statistics.value = res.data.statistics
      resultImageUrl.value = res.data.resultImageUrl
      ElMessage.success(`检测完成，发现 ${statistics.value?.total || 0} 个目标`)
    } else { ElMessage.error(res.msg) }
  } catch { ElMessage.error('检测请求失败') }
  finally { detecting.value = false }
}
</script>

<style scoped>
.layout { background:#f5f7fa; min-height:100vh; }
.top-nav { padding:0 20px; }
.flex-grow { flex-grow:1; }
.main-content { padding:20px; max-width:1300px; margin:0 auto; }
.upload-area { min-height:200px; display:flex; align-items:center; justify-content:center; }
.upload-placeholder { text-align:center; cursor:pointer; padding:40px; border:2px dashed #ddd; border-radius:8px; width:100%; transition:all .3s; }
.upload-placeholder:hover { border-color:#2a9d8f; background:#f0fdf6; }
.upload-progress { text-align:center; padding:30px; }
.upload-preview { position:relative; cursor:pointer; }
.upload-overlay { position:absolute; bottom:0; left:0; right:0; background:rgba(0,0,0,0.6); color:#fff; text-align:center; padding:6px; font-size:13px; border-radius:0 0 6px 6px; }
</style>
