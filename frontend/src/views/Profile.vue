<template>
  <el-row :gutter="20">
    <!-- 左：基本信息 -->
    <el-col :span="12">
      <el-card>
        <template #header>
          <span>基本信息</span>
        </template>

        <el-form :model="profileForm" label-width="100px">
          <el-form-item label="用户名">
            <el-input :model-value="userStore.username" disabled />
          </el-form-item>

          <el-form-item label="角色">
            <el-tag :type="userStore.isAdmin ? 'danger' : 'success'">
              {{ userStore.isAdmin ? '管理员' : '企业用户' }}
            </el-tag>
          </el-form-item>

          <el-form-item label="工厂名称">
            <el-input v-model="profileForm.factoryName" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="profileLoading" @click="saveProfile">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>

    <!-- 右：修改密码 -->
    <el-col :span="12">
      <el-card>
        <template #header>
          <span>修改密码</span>
        </template>

        <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="100px"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
                v-model="pwdForm.oldPassword"
                type="password"
                show-password
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
                v-model="pwdForm.newPassword"
                type="password"
                show-password
            />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
                v-model="pwdForm.confirmPassword"
                type="password"
                show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="pwdLoading" @click="savePassword">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userstore.js'
import { getCurrentUserApi, updateProfileApi, updatePasswordApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// ====== 基本信息 ======
const profileForm = reactive({
  factoryName: ''
})
const profileLoading = ref(false)

async function loadProfile() {
  try {
    const res = await getCurrentUserApi()
    profileForm.factoryName = res.data.factoryName
  } catch (e) { /* 拦截器已提示 */ }
}

async function saveProfile() {
  profileLoading.value = true
  try {
    await updateProfileApi({ factoryName: profileForm.factoryName })
    ElMessage.success('保存成功')
    // 更新本地存储
    localStorage.setItem('factoryName', profileForm.factoryName)
    userStore.factoryName = profileForm.factoryName
  } catch (e) { /* 拦截器已提示 */ }
  finally { profileLoading.value = false }
}

// ====== 修改密码 ======
const pwdFormRef = ref()
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwdLoading = ref(false)

function validateConfirm(rule, value, callback) {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function savePassword() {
  await pwdFormRef.value.validate()

  pwdLoading.value = true
  try {
    await updatePasswordApi(pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (e) { /* 拦截器已提示 */ }
  finally { pwdLoading.value = false }
}

onMounted(loadProfile)
</script>