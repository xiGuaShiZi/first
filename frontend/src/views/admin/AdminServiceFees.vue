<template>
  <div class="admin-service-fees">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>平台服务费管理</h2>
        </div>
      </template>

      <!-- 费率配置表 -->
      <div style="margin-bottom: 24px;">
        <h3 style="margin-bottom: 12px; font-size: 16px;">商家等级费率配置</h3>
        <el-table :data="feeRateList" stripe border style="width: 100%">
          <el-table-column prop="level" label="商家等级" width="150">
            <template #default="{ row }">
              <el-rate v-model="row.level" disabled />
            </template>
          </el-table-column>
            <el-table-column prop="rate" label="交易费率" width="150">
              <template #default="{ row }">
                {{ (parseRate(row.rate) * 100).toFixed(3) }}% ({{ row.rate }})
              </template>
            </el-table-column>
        </el-table>
      </div>

      <!-- 服务费记录 -->
      <h3 style="margin-bottom: 12px; font-size: 16px;">服务费记录</h3>
      <el-table :data="serviceFees" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shopName" label="商家" width="150">
          <template #default="{ row }">
            <span :title="row.realName">{{ row.shopName || '商家#' + row.merchantId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="200" />
        <el-table-column prop="orderAmount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ Number(row.orderAmount).toFixed(2) }}
          </template>
        </el-table-column>
          <el-table-column label="商家等级" width="150">
           <template #default="{ row }">
             <el-rate :model-value="getLevel(row)" disabled />
           </template>
         </el-table-column>
            <el-table-column prop="feeRate" label="费率" width="100">
              <template #default="{ row }">
                {{ (getRowRate(row) * 100).toFixed(3) }}% (raw: {{ row.feeRate ?? 'null' }})
              </template>
            </el-table-column>
        <el-table-column prop="feeAmount" label="服务费" width="120">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: bold;">¥{{ Number(row.feeAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        @current-change="loadData"
        @size-change="loadData"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api/modules'

const loading = ref(false)
const serviceFees = ref([])
const feeRateList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.serviceFees({ page: currentPage.value, size: pageSize.value })
    // adminApi may return { data: { content, totalElements } } or the data object directly
    const payload = res?.data || res || {}
    serviceFees.value = payload.content || payload || []
    total.value = payload.totalElements || payload.total || 0
  } finally {
    loading.value = false
  }
}

const loadFeeRates = async () => {
  try {
    const res = await adminApi.feeRates()
    const data = res?.data || res || {}
    console.log('feeRates raw response:', res)
    console.log('feeRates normalized data:', data)
    // Accept either an object map { "1": 0.05 } or an array [{ level:1, rate:0.05 }]
    if (Array.isArray(data)) {
      feeRateList.value = data.map(item => ({ level: Number(item.level), rate: Number(item.rate) }))
    } else {
      feeRateList.value = Object.keys(data).map(key => ({ level: Number(key), rate: Number(data[key]) }))
    }
    // sort by level asc
    feeRateList.value.sort((a, b) => a.level - b.level)
    console.log('feeRateList:', feeRateList.value)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载费率配置失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// robustly parse various shapes of numeric fee rate returned from backend
const parseRate = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  if (typeof v === 'string') {
    const n = Number(v)
    if (!isNaN(n)) return n
  }
  if (typeof v === 'object') {
    if (v.value != null) {
      const n = Number(v.value)
      if (!isNaN(n)) return n
    }
    if (v.bigDecimal != null) {
      const n = Number(v.bigDecimal)
      if (!isNaN(n)) return n
    }
    if (v.unscaledValue != null && v.scale != null) {
      const unscaled = Number(v.unscaledValue)
      const scale = Number(v.scale) || 0
      if (!isNaN(unscaled)) return unscaled / Math.pow(10, scale)
    }
    // fallback: try to coerce to number
    const asNum = Number(String(v))
    if (!isNaN(asNum)) return asNum
  }
  return 0
}
// 获取商家等级：优先使用 merchantLevel，为 null/0 时从 feeRate 反推
const getLevel = (row) => {
  if (row.merchantLevel && row.merchantLevel > 0) return row.merchantLevel
  const rate = parseRate(row?.feeRate)
  // 数值匹配（带容差，避免浮点精度问题）
  if (Math.abs(rate - 0.001) < 0.0001) return 1
  if (Math.abs(rate - 0.002) < 0.0001) return 2
  if (Math.abs(rate - 0.005) < 0.0001) return 3
  if (Math.abs(rate - 0.0075) < 0.0001) return 4
  if (Math.abs(rate - 0.01) < 0.0001) return 5
  return 0
}
// Compute a sensible rate for a service-fee row: prefer explicit row.feeRate,
// otherwise fall back to feeAmount / orderAmount when available.
const getRowRate = (row) => {
  const explicit = parseRate(row?.feeRate)
  if (explicit && explicit > 0) return explicit
  // fallback compute
  const orderAmount = Number(row?.orderAmount)
  const feeAmount = Number(row?.feeAmount)
  if (orderAmount && !isNaN(orderAmount) && feeAmount && !isNaN(feeAmount)) {
    // avoid division by zero
    return feeAmount / orderAmount
  }
  return 0
}

onMounted(() => {
  loadData()
  loadFeeRates()
})
</script>

<style scoped>
.admin-service-fees {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}
</style>
