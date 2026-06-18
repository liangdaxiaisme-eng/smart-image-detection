<template>
  <div class="register-page">
    <div class="register-box">
      <h1 class="title">注册账号</h1>
      <el-form :model="form" label-width="0">
        <el-input v-model="form.username" placeholder="用户名" size="large" clearable />
        <el-input v-model="form.nickname" placeholder="昵称" size="large" clearable style="margin-top:15px" />
        <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password style="margin-top:15px" />
        <el-input v-model="form.confirm" type="password" placeholder="确认密码" size="large" show-password style="margin-top:15px" />
        <el-button type="primary" size="large" style="width:100%;margin-top:20px" @click="register" :loading="loading">注 册</el-button>
      </el-form>
      <div class="login-link">已有账号？<router-link to="/login">去登录</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const form = ref({ username: '', nickname: '', password: '', confirm: '' })
const loading = ref(false)

const register = async () => {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请填写完整信息')
  if (form.value.password !== form.value.confirm) return ElMessage.warning('两次密码不一致')
  loading.value = true
  try {
    const res = await api.post('/user/register', { username: form.value.username, password: form.value.password, nickname: form.value.nickname })
    if (res.code === 200) { ElMessage.success('注册成功'); router.push('/login') }
    else ElMessage.error(res.msg)
  } catch { ElMessage.error('注册失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.register-page { display:flex; justify-content:center; align-items:center; height:100vh; background:linear-gradient(135deg, #264653, #2a9d8f); }
.register-box { width:420px; padding:40px; background:#fff; border-radius:12px; box-shadow:0 10px 40px rgba(0,0,0,0.2); }
.title { text-align:center; font-size:24px; color:#264653; margin-bottom:30px; }
.login-link { text-align:center; margin-top:20px; color:#999; }
.login-link a { color:#2a9d8f; text-decoration:none; }
</style>
