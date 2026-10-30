<template>
  <div class="supplier-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="供应商名称">
          <el-input
              v-model="query.name"
              placeholder="请输入名称"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="所属行业">
          <el-input
              v-model="query.industry"
              placeholder="请输入行业"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="绿色等级">
          <el-select v-model="query.greenLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="A（优秀）" value="A" />
            <el-option label="B（合格）" value="B" />
            <el-option label="C（整改）" value="C" />
          </el-select>
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
          <span>供应商列表</span>
          <div>
            <el-button @click="handleRecalculate" :loading="recalcLoading">
              <el-icon><RefreshRight /></el-icon> 重算绿色等级
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增供应商
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe empty-text="暂无数据">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="供应商名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" width="120" />
        <el-table-column prop="industry" label="所属行业" width="130" />
        <el-table-column label="绿色等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.greenLevel" :type="getTagType(row.greenLevel)" effect="dark">
              {{ row.greenLevel }} {{ getLevelText(row.greenLevel) }}
            </el-tag>
            <el-tag v-else type="info" effect="plain">未评级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="avgEmission" label="近6月平均排放" width="160" align="right">
          <template #default="{ row }">
            <span v-if="row.avgEmission != null">{{ Number(row.avgEmission).toFixed(2) }} tCO₂e</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button type="success" link @click="handleBind(row)">绑定碳排</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @closed="handleDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="所属行业" prop="industry">
          <el-input v-model="form.industry" placeholder="如：电力、制造、化工" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="供应商详情" size="60%" @closed="detail = null">
      <div v-if="detail" v-loading="detailLoading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="供应商名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ detail.contact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属行业">{{ detail.industry || '-' }}</el-descriptions-item>
          <el-descriptions-item label="绿色等级">
            <el-tag v-if="detail.greenLevel" :type="getTagType(detail.greenLevel)" effect="dark">
              {{ detail.greenLevel }} {{ getLevelText(detail.greenLevel) }}
            </el-tag>
            <el-tag v-else type="info" effect="plain">未评级</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="近6月平均排放">
            {{ detail.avgEmission != null ? Number(detail.avgEmission).toFixed(2) + ' tCO₂e' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section-title">近6个月绑定的碳排记录</div>
        <el-table :data="detail.records || []" border stripe empty-text="暂无绑定记录">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="reportMonth" label="上报月份" width="110" />
          <el-table-column prop="energyType" label="能源类型" width="120" />
          <el-table-column prop="consumeAmount" label="消耗量" width="120" align="right" />
          <el-table-column prop="totalEmission" label="排放量(tCO₂e)" width="140" align="right">
            <template #default="{ row }">{{ Number(row.totalEmission).toFixed(4) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button type="danger" link @click="handleUnbind(row)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 绑定碳排记录弹窗 -->
    <el-dialog v-model="bindVisible" title="绑定碳排记录" width="800px" @closed="handleBindClosed">
      <div class="bind-tip">
        当前供应商：<b>{{ currentSupplier?.name }}</b>，仅可绑定「审核通过」且未绑定其他供应商的碳排记录。
      </div>
      <el-form :inline="true" :model="bindQuery" class="bind-search">
        <el-form-item label="上报月份">
          <el-date-picker
              v-model="bindQuery.reportMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="选择月份"
              clearable
              @change="loadAvailableRecords"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAvailableRecords">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
          ref="bindTableRef"
          :data="availableRecords"
          v-loading="bindLoading"
          border
          stripe
          empty-text="暂无可绑定的碳排记录"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="reportMonth" label="上报月份" width="110" />
        <el-table-column prop="energyType" label="能源类型" width="120" />
        <el-table-column prop="consumeAmount" label="消耗量" width="120" align="right" />
        <el-table-column prop="totalEmission" label="排放量(tCO₂e)" width="150" align="right">
          <template #default="{ row }">{{ Number(row.totalEmission).toFixed(4) }}</template>
        </el-table-column>
        <el-table-column prop="factoryName" label="所属工厂" min-width="140" show-overflow-tooltip />
      </el-table>

      <template #footer>
        <el-button @click="bindVisible = false">取消</el-button>
        <el-button
            type="primary"
            :loading="bindSubmitLoading"
            :disabled="selectedRecords.length === 0"
            @click="handleBindSubmit"
        >
          确认绑定（已选 {{ selectedRecords.length }} 条）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, RefreshRight } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getSupplierPageApi,
  getSupplierByIdApi,
  saveSupplierApi,
  updateSupplierApi,
  deleteSupplierApi,
  bindSupplierRecordsApi,
  unbindSupplierRecordApi,
  recalculateGreenLevelApi
} from '@/api/supplier'
import { listBindable   } from '@/api/carbonRecord' // 需你已有：分页查询碳排记录

/* ==================== 列表 ==================== */
const query = reactive({ page: 1, size: 10, name: '', industry: '', greenLevel: '' })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await getSupplierPageApi(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadData()
}
function handleReset() {
  query.name = ''
  query.industry = ''
  query.greenLevel = ''
  query.page = 1
  loadData()
}

/* ==================== 新增/编辑 ==================== */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', contact: '', industry: '', remark: '' })
const dialogTitle = computed(() => (form.id ? '编辑供应商' : '新增供应商'))

const rules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { max: 100, message: '不超过 100 字符', trigger: 'blur' }
  ]
}

function handleAdd() {
  Object.assign(form, { id: null, name: '', contact: '', industry: '', remark: '' })
  dialogVisible.value = true
}

async function handleEdit(row) {
  // 用详情接口取最新数据
  try {
    const res = await getSupplierByIdApi(row.id)
    const d = res.data
    Object.assign(form, {
      id: d.id,
      name: d.name,
      contact: d.contact,
      industry: d.industry,
      remark: d.remark
    })
  } catch (e) {
    Object.assign(form, { ...row })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateSupplierApi(form)
      ElMessage.success('修改成功')
    } else {
      await saveSupplierApi(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    submitLoading.value = false
  }
}

function handleDialogClosed() {
  formRef.value?.resetFields()
}

/* ==================== 删除 ==================== */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
        `确定删除供应商「${row.name}」吗？该操作不可恢复。`,
        '提示',
        { type: 'warning' }
    )
    await deleteSupplierApi(row.id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && query.page > 1) query.page--
    loadData()
  } catch (e) {
    /* 取消 */
  }
}

/* ==================== 详情抽屉 ==================== */
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

async function handleView(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await getSupplierByIdApi(row.id)
    detail.value = res.data
  } catch (e) {
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function handleUnbind(record) {
  try {
    await ElMessageBox.confirm(
        `确定解绑该碳排记录（${record.reportMonth} ${record.energyType}）吗？`,
        '提示',
        { type: 'warning' }
    )
    await unbindSupplierRecordApi(record.id)
    ElMessage.success('解绑成功')
    // 刷新详情
    if (detail.value) {
      const res = await getSupplierByIdApi(detail.value.id)
      detail.value = res.data
    }
    loadData()
  } catch (e) {
    /* 取消 */
  }
}

/* ==================== 绑定碳排记录 ==================== */
const bindVisible = ref(false)
const bindLoading = ref(false)
const bindSubmitLoading = ref(false)
const bindTableRef = ref()
const availableRecords = ref([])
const selectedRecords = ref([])
const currentSupplier = ref(null)
const bindQuery = reactive({ reportMonth: '' })

function handleBind(row) {
  currentSupplier.value = row
  bindQuery.reportMonth = ''
  availableRecords.value = []
  selectedRecords.value = []
  bindVisible.value = true
  loadAvailableRecords()
}

async function loadAvailableRecords() {
  bindLoading.value = true
  try {
    const res = await listBindable(bindQuery.reportMonth || undefined)

    // 注意：后端返回的是 Result<List<CarbonRecordVO>>，所以是数组，不是分页对象
    availableRecords.value = res.data || []
  } catch (e) {
    availableRecords.value = []
  } finally {
    bindLoading.value = false
  }
}

function handleSelectionChange(rows) {
  selectedRecords.value = rows
}

async function handleBindSubmit() {
  if (selectedRecords.value.length === 0) return
  bindSubmitLoading.value = true
  try {
    await bindSupplierRecordsApi({
      supplierId: currentSupplier.value.id,
      recordIds: selectedRecords.value.map(r => r.id)
    })
    ElMessage.success('绑定成功')
    bindVisible.value = false
    loadData()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    bindSubmitLoading.value = false
  }
}

function handleBindClosed() {
  bindTableRef.value?.clearSelection()
  selectedRecords.value = []
  currentSupplier.value = null
}

/* ==================== 手动重算等级 ==================== */
const recalcLoading = ref(false)
async function handleRecalculate() {
  try {
    await ElMessageBox.confirm(
        '将根据近6个月审核通过的碳排记录重新计算所有供应商的绿色等级，是否继续？',
        '提示',
        { type: 'warning' }
    )
    recalcLoading.value = true
    await recalculateGreenLevelApi()
    ElMessage.success('绿色等级重算完成')
    loadData()
  } catch (e) {
    /* 取消 */
  } finally {
    recalcLoading.value = false
  }
}

/* ==================== 工具函数 ==================== */
function formatTime(time) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''
}

function getTagType(level) {
  return { A: 'success', B: 'warning', C: 'danger' }[level] || 'info'
}
function getLevelText(level) {
  return { A: '优秀', B: '合格', C: '整改' }[level] || ''
}
function getStatusType(status) {
  return { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info'
}
function getStatusText(status) {
  return { 0: '草稿', 1: '待审核', 2: '审核通过', 3: '驳回' }[status] || '未知'
}

/* ==================== 生命周期 ==================== */
onMounted(loadData)
</script>

<style scoped>
.supplier-page {
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
.detail-section-title {
  margin: 20px 0 12px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.bind-tip {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}
.bind-search {
  margin-bottom: 8px;
}
</style>