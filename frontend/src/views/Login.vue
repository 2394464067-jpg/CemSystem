<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>碳排数据管理系统</h2>
        <p>Carbon Emission Management System</p>
      </div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
          size="large"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              style="width: 100%"
              @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        还没有账号？
        <el-link type="primary" @click="$router.push('/register')">
          立即注册
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userstore.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度 6-20 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  // 1. 表单校验
  await formRef.value.validate()

  // 2. 提交
  loading.value = true
  try {
    const userData = await userStore.login(form)

    ElMessage.success('登录成功')

    // 3. 跳转：优先跳回原来想去的页面
    let redirect = route.query.redirect
    if (!redirect) {
      redirect = userStore.isAdmin ? '/dashboard' : '/carbon'
    }
    router.push(redirect)

  } catch (e) {
    // 错误提示已由拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #4caf50 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #1e3c72;
  font-size: 24px;
  margin-bottom: 8px;
}

.login-header p {
  color: #999;
  font-size: 12px;
  letter-spacing: 1px;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-top: 10px;
}
</style>