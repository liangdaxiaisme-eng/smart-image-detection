<template>
  <div class="login-page">
    <div class="login-box">
      <h1 class="title">智能图像检测管理系统</h1>
      <p class="subtitle">SpringBoot + YOLO 课程设计</p>
      <el-form :model="form" label-width="0" class="login-form">
        <el-input v-model="form.username" placeholder="用户名" size="large" clearable />
        <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password style="margin-top:15px" />
        <el-button type="primary" size="large" style="width:100%;margin-top:20px" @click="login" :loading="loading">登 录</el-button>
      </el-form>
      <div class="register-link">
        没有账号？<router-link to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const login = async () => {
  if (!form.value.username || !form.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    const res = await api.post('/user/login', form.value)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.msg)
    }
  } catch (err) {
    ElMessage.error('登录失败，请检查网络')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { display:flex; justify-content:center; align-items:center; height:100vh; background:linear-gradient(135deg, #264653, #2a9d8f); }
.login-box { width:420px; padding:40px; background:#fff; border-radius:12px; box-shadow:0 10px 40px rgba(0,0,0,0.2); }
.title { text-align:center; font-size:24px; color:#264653; margin-bottom:5px; }
.subtitle { text-align:center; color:#999; margin-bottom:30px; font-size:14px; }
.register-link { text-align:center; margin-top:20px; color:#999; font-size:14px; }
.register-link a { color:#2a9d8f; text-decoration:none; }
</style>
