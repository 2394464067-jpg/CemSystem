<template>
  <div class="dashboard-page" v-loading="loading">
    <!-- 顶部指标卡 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">累计总排放</div>
          <div class="stat-value">{{ formatNum(data.totalEmission) }} <span class="unit">tCO₂e</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">本月排放</div>
          <div class="stat-value">{{ formatNum(data.currentMonthEmission) }} <span class="unit">tCO₂e</span></div>
          <div class="stat-sub" :class="rateClass">
            环比 {{ data.monthOverMonthRate > 0 ? '+' : '' }}{{ data.monthOverMonthRate ?? 0 }}%
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">供应商总数</div>
          <div class="stat-value">{{ data.supplierCount ?? 0 }} <span class="unit">家</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">待审核记录</div>
          <div class="stat-value warning">{{ data.pendingCount ?? 0 }} <span class="unit">条</span></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势折线图 -->
    <el-card shadow="never" class="chart-card">
      <template #header><span>近12个月碳排放趋势</span></template>
      <div ref="trendRef" class="chart"></div>
    </el-card>

    <!-- 两个饼图 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>各工厂排放占比</span></template>
          <div ref="factoryRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>各供应商排放占比</span></template>
          <div ref="supplierRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 导出区 -->
    <el-card shadow="never" class="export-card">
      <template #header><span>碳排明细报表导出</span></template>
      <el-form :inline="true" :model="exportQuery">
        <el-form-item label="起始月份">
          <el-date-picker v-model="exportQuery.startMonth" type="month" value-format="YYYY-MM" placeholder="必选" />
        </el-form-item>
        <el-form-item label="结束月份">
          <el-date-picker v-model="exportQuery.endMonth" type="month" value-format="YYYY-MM" placeholder="可选" />
        </el-form-item>
        <el-form-item label="工厂名称">
          <el-input v-model="exportQuery.factoryName" placeholder="可选" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="exportLoading" @click="handleExport">
            <el-icon><Download /></el-icon> 导出 Excel
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getDashboardApi } from '@/api/dashboard'
import { exportCarbonReportApi } from '@/api/report'

const loading = ref(false)
const data = ref({})

const trendRef = ref()
const factoryRef = ref()
const supplierRef = ref()
let trendChart = null, factoryChart = null, supplierChart = null

const rateClass = computed(() => {
  const r = data.value.monthOverMonthRate ?? 0
  if (r > 0) return 'up'
  if (r < 0) return 'down'
  return ''
})

function formatNum(v) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/* ============ 加载看板 ============ */
async function loadDashboard() {
  loading.value = true
  try {
    const res = await getDashboardApi()
    data.value = res.data || {}
    await nextTick()
    renderTrend()
    renderFactoryPie()
    renderSupplierPie()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

/* ============ 折线图 ============ */
function renderTrend() {
  const trend = data.value.monthlyTrend || []
  const months = trend.map(i => i.month)
  const values = trend.map(i => Number(i.totalEmission ?? 0))

  if (!trendChart) trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis', valueFormatter: v => `${Number(v).toFixed(2)} tCO₂e` },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: months, boundaryGap: false },
    yAxis: { type: 'value', name: 'tCO₂e' },
    series: [{
      name: '碳排放',
      type: 'line',
      smooth: true,
      data: values,
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

/* ============ 饼图（通用） ============ */
function renderPie(chartRef, chartInstance, pieData, colorList) {
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  const arr = (pieData || []).map(i => ({ name: i.name, value: Number(i.value ?? 0) }))
  chartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} tCO₂e ({d}%)' },
    legend: { type: 'scroll', orient: 'vertical', right: 10, top: 20, bottom: 20 },
    color: colorList,
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: true,
      label: { formatter: '{b}\n{d}%' },
      data: arr
    }]
  })
  return chartInstance
}

const FACTORY_COLORS = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9B59B6', '#1ABC9C']
const SUPPLIER_COLORS = ['#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272', '#FC8452']

function renderFactoryPie() {
  factoryChart = renderPie(factoryRef, factoryChart, data.value.factoryPie, FACTORY_COLORS)
}
function renderSupplierPie() {
  supplierChart = renderPie(supplierRef, supplierChart, data.value.supplierPie, SUPPLIER_COLORS)
}

/* ============ 窗口自适应 ============ */
function handleResize() {
  trendChart?.resize()
  factoryChart?.resize()
  supplierChart?.resize()
}

/* ============ 导出 ============ */
const exportQuery = reactive({ startMonth: '', endMonth: '', factoryName: '' })
const exportLoading = ref(false)

async function handleExport() {
  if (!exportQuery.startMonth) {
    ElMessage.warning('请选择起始月份')
    return
  }
  exportLoading.value = true
  try {
    const res = await exportCarbonReportApi({
      startMonth: exportQuery.startMonth,
      endMonth: exportQuery.endMonth || undefined,
      factoryName: exportQuery.factoryName || undefined
    })

    // res 是 blob（视你的 request 封装而定：可能 res.data 是 blob，也可能 res 本身就是 blob）
    const blob = res instanceof Blob ? res : new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `碳排明细报表_${exportQuery.startMonth}${exportQuery.endMonth ? '_至_' + exportQuery.endMonth : ''}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

/* ============ 生命周期 ============ */
onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  factoryChart?.dispose()
  supplierChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.stat-row .stat-card {
  border-radius: 8px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}
.stat-value.warning { color: #E6A23C; }
.stat-value .unit {
  font-size: 13px;
  font-weight: normal;
  color: #909399;
}
.stat-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.stat-sub.up { color: #F56C6C; }
.stat-sub.down { color: #67C23A; }
.chart-card {
  border-radius: 8px;
}
.chart {
  width: 100%;
  height: 320px;
}
.export-card {
  border-radius: 8px;
}
</style>