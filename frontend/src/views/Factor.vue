<template>
  <div class="factor-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="能源类型">
          <el-input
              v-model="query.energyName"
              placeholder="请输入能源类型"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>排放因子列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增因子
          </el-button>
        </div>
      </template>

      <!-- 和tableData绑定数据 -->
      <el-table
          :data="tableData"
          v-loading="loading"
          border
          stripe
          empty-text="暂无数据"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="energyName" label="能源类型" min-width="120" />
        <el-table-column prop="factorValue" label="排放因子" min-width="120" align="right">
          <template #default="{ row }">
            {{ row.factorValue }} kgCO₂e
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="上次更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
          @size-change="loadData"
          @current-change="loadData"
      />
    </el-card>

    <!-- 新增/修改弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
        @closed="handleDialogClosed"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="能源类型" prop="energyName">
          <el-input
              v-model="form.energyName"
              placeholder="如：电力、天然气、柴油、蒸汽"
          />
        </el-form-item>

        <el-form-item label="排放因子" prop="factorValue">
          <el-input-number
              v-model="form.factorValue"
              :precision="4"
              :min="0.0001"
              :step="0.01"
              style="width: 100%"
              placeholder="请输入因子值"
          />
        </el-form-item>

        <el-form-item label="单位" prop="unit">
          <el-input
              v-model="form.unit"
              placeholder="如：kgCO2/kWh"
          />
        </el-form-item>

      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getFactorPageApi,
  saveFactorApi,
  updateFactorApi,
  deleteFactorApi
} from '@/api/factor'

// ========== 查询参数 ==========
const query = reactive({
  page: 1,
  size: 10,
  energyName: ''
})

// ========== 表格数据 ==========
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// ========== 加载数据 ==========
async function loadData() {
  loading.value = true
  try {
    const res = await getFactorPageApi(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

// ========== 查询/重置 ==========
function handleSearch() {
  query.page = 1
  loadData()
}

function handleReset() {
  query.energyName = ''
  query.page = 1
  loadData()
}

// ========== 弹窗 ==========
const dialogVisible = ref(false)
const dialogTitle = computed(() => (form.id ? '编辑因子' : '新增因子'))
const submitLoading = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  energyName: '',
  factorValue: null,
  unit: ''
})

const rules = {
  energyName: [
    { required: true, message: '请输入能源类型', trigger: 'blur' },
    { max: 50, message: '不超过 50 字符', trigger: 'blur' }
  ],
  factorValue: [
    { required: true, message: '请输入因子值', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined) {
          callback(new Error('请输入因子值'))
        } else if (Number(value) <= 0) {
          callback(new Error('因子值必须为正数'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}

// ========== 新增 ==========
function handleAdd() {
  // 重置表单
  Object.assign(form, {
    id: null,
    energyName: '',
    factorValue: null,
    unit: ''
  })
  dialogVisible.value = true
}

// ========== 修改 ==========
function handleEdit(row) {
  Object.assign(form, {
    id: row.id,
    energyName: row.energyName,
    factorValue: Number(row.factorValue),
    unit: row.unit
  })
  dialogVisible.value = true
}

// ========== 提交 ==========
async function handleSubmit() {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    if (form.id) {
      await updateFactorApi(form)
      ElMessage.success('修改成功')
    } else {
      await saveFactorApi(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitLoading.value = false
  }
}

// ========== 弹窗关闭后重置 ==========
function handleDialogClosed() {
  formRef.value?.resetFields()
}

// ========== 删除 ==========
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
        `确定删除「${row.energyName}」这个排放因子吗？`,
        '提示',
        { type: 'warning' }
    )
    await deleteFactorApi(row.id)
    ElMessage.success('删除成功')
    // 如果当前页只有一条，删除后回到上一页
    if (tableData.value.length === 1 && query.page > 1) {
      query.page--
    }
    loadData()
  } catch (e) {
    // 取消 或 接口异常
  }
}

// ========== 时间格式化 ==========
function formatTime(time) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''
}

// ========== 生命周期 ==========
onMounted(loadData)
</script>

<style scoped>
.factor-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>