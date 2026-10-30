<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>注册账号</h2>
        <p>加入碳排数据管理系统</p>
      </div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="90px"
          size="large"
      >

        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="form.username"
              placeholder="3-20 个字符"
              clearable
          />
        </el-form-item>  <!-- 校验规则按 prop 字段名去 model 里找值标记表单数据 -->

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="6-20 个字符"
              show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
          />
        </el-form-item>

        <el-form-item label="工厂名称" prop="factoryName">
          <el-input
              v-model="form.factoryName"
              placeholder="请输入所属工厂/企业名称"
              clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              style="width: 100%"
              @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        已有账号？
        <el-link type="primary" @click="$router.push('/login')">
          返回登录
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userstore.js'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

//定义数据源对象
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  factoryName: ''
})

// 自定义校验：两次密码一致
function validateConfirm(rule, value, callback) {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

//定义校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  factoryName: [
    { required: true, message: '请输入工厂名称', trigger: 'blur' }
  ]
}

// 点击注册按钮
async function handleRegister() {
  await formRef.value.validate() //拿到formRef实例并进行校验

  loading.value = true
  try {
    await userStore.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // axios拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #4caf50 100%);
  overflow: auto;
}

.register-box {
  width: 520px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  margin: 20px 0;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #1e3c72;
  font-size: 24px;
  margin-bottom: 8px;
}

.register-header p {
  color: #999;
  font-size: 12px;
  letter-spacing: 1px;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>