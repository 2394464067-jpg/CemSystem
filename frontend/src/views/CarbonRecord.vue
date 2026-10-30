<template>
  <div class="app-container">
    <el-card>
      <!-- 查询 -->
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="月份">
          <el-date-picker
              v-model="query.reportMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="选择月份"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 工具栏:新增上报 -->
      <div class="toolbar">
        <el-button v-if="!isAdmin" type="primary" @click="openDialog()">新增上报</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column v-if="isAdmin" prop="userId" label="工厂用户编号" width="110" />
        <el-table-column v-if="isAdmin" prop="factoryName" label="所属工厂名称" width="110" />
        <el-table-column prop="reportMonth" label="月份" width="110" />
        <el-table-column prop="energyType" label="能源类型" width="120" />
        <el-table-column prop="consumeAmount" label="消耗量" width="120" />
        <el-table-column prop="totalEmission" label="碳排放量(kgCO₂)" width="160" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.label || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <!-- 草稿 -->
            <template v-if="!isAdmin && row.status === 0">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="success" @click="handleSubmit(row)">提交审核</el-button>
            </template>

            <!-- 驳回 -->
            <template v-if="!isAdmin && row.status === 3">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="warning" @click="handleResubmit(row)">重新提交</el-button>
            </template>

            <!-- 管理员：待审核 -->
            <template v-if="isAdmin && row.status === 1">
              <el-button link type="success" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
            </template>

            <!-- 管理员：草稿/驳回可删 -->
            <el-button
                v-if="isAdmin && (row.status === 0 || row.status === 3)"
                link type="danger"
                @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          class="pager"
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchList"
          @size-change="handleSizeChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑碳排记录' : '新增碳排记录'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="上报月份" prop="reportMonth">
          <el-date-picker
              v-model="form.reportMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="选择月份"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="能源类型" prop="energyType">
          <el-select v-model="form.energyType" placeholder="请选择" style="width: 100%">
            <el-option
                v-for="f in factors"
                :key="f.id"
                :label="f.energyName"
                :value="f.energyName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="消耗量" prop="consumeAmount">
          <el-input-number v-model="form.consumeAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预估碳排放">
          <el-input :model-value="computedEmission" disabled />
          <div class="tip">前端仅做预估展示，最终以后端核算为准</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" @click="saveAndSubmit">保存并提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listCarbon, saveDraftApi, submitApi, approveApi, rejectApi, reSubmitApi, deleteCarbon, saveAndSubmitApi
} from '@/api/carbonRecord.js'
import { getFactorPageApi } from '@/api/factor'
import { useUserStore } from '@/stores/userstore.js'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '待审核' },
  { value: 2, label: '审核通过' },
  { value: 3, label: '已驳回' }
]
const statusMap = {
  0: { label: '草稿', type: 'info' },
  1: { label: '待审核', type: 'warning' },
  2: { label: '审核通过', type: 'success' },
  3: { label: '已驳回', type: 'danger' }
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const factors = ref([])



const query = reactive({ reportMonth: '', status: '', page: 1, size: 10 })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  reportMonth: '',
  energyType: '',
  consumeAmount: 0,
})

const rules = {
  reportMonth: [{ required: true, message: '请选择月份', trigger: 'change' }],
  energyType: [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  consumeAmount: [{ required: true, message: '请输入消耗量', trigger: 'blur' }]
}

const computedEmission = computed(() => {
  const f = factors.value.find(i => i.energyName === form.energyType)
  if (!f || form.consumeAmount == null) return '—'
  return (form.consumeAmount * f.factorValue).toFixed(4) + ' kgCO₂'
})

async function fetchList() {
  loading.value = true
  try {
    const res = await listCarbon(query)
    const data = res.data || {}
    list.value = data.records || data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadRefs() {
  const [f] = await Promise.all([
    getFactorPageApi({ page: 1, size: 1000 })
  ])
  factors.value = f.data?.records || f.data?.list || []
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleSizeChange() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.reportMonth = ''
  query.status = ''
  handleSearch()
}

//新增&编辑
function openDialog(row) {
  if (row) {
    Object.assign(form, {
      id: row.id,
      reportMonth: row.reportMonth,
      energyType: row.energyType,
      consumeAmount: row.consumeAmount,
    })
  } else {
    Object.assign(form, {
      id: null, reportMonth: '', energyType: '', consumeAmount: 0
    })
  }
  dialogVisible.value = true
}


function buildPayload() {
  const payload = {
    reportMonth: form.reportMonth,
    energyType: form.energyType,
    consumeAmount: form.consumeAmount
  }
  if (form.id) payload.id = form.id
  return payload
}

async function validateForm() {
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

//保存草稿
async function saveDraft() {
  if (!(await validateForm())) return
  await saveDraftApi(buildPayload())
  ElMessage.success('已保存草稿')
  dialogVisible.value = false
  fetchList()
}

//保存并提交
async function saveAndSubmit() {
  if (!(await validateForm())) return
  await saveAndSubmitApi(buildPayload())
  ElMessage.success('已提交审核')
  dialogVisible.value = false
  fetchList()
}

//提交审核
async function handleSubmit(row) {
  await ElMessageBox.confirm('确认提交审核？提交后不可修改', '提示', { type: 'warning' })
  await submitApi(row.id)
  ElMessage.success('提交成功')
  fetchList()
}
//审核通过
async function handleApprove(row) {
  await approveApi(row.id)
  ElMessage.success('审核通过')
  fetchList()
}
//驳回
async function handleReject(row) {
  await rejectApi(row.id)
  ElMessage.success('审核驳回')
  fetchList()
}
//重新提交
async function handleResubmit(row) {
  await ElMessageBox.confirm('确认重新提交？', '提示', { type: 'warning' })
  await reSubmitApi(row.id)
  ElMessage.success('已重新提交')
  fetchList()
}
//删除
async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await deleteCarbon(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(() => {
  fetchList()
  loadRefs()
})
</script>

<style scoped>
.app-container { padding: 16px; }
.toolbar { margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
.tip { font-size: 12px; color: #909399; }
</style>