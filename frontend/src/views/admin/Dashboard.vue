<template>
  <section class="admin-page">
    <div class="admin-heading">
      <div>
        <h1>后台概览</h1>
        <p>查看校园贴士、交易金额和闲置物品运营统计。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="load">刷新数据</el-button>
    </div>
    <div class="stat-row">
      <div><strong>{{ stats.newsTotal || 0 }}</strong><span>校园贴士</span></div>
      <div><strong>{{ stats.productTotal || 0 }}</strong><span>闲置物品</span></div>
      <div><strong>{{ stats.messageTotal || 0 }}</strong><span>咨询总数</span></div>
      <div><strong>{{ stats.pendingMessageTotal || 0 }}</strong><span>待处理咨询</span></div>
    </div>

    <div class="admin-heading statistics-head">
      <div>
        <h2>交易统计</h2>
        <p>{{ orderStats.startDate }} 至 {{ orderStats.endDate }}</p>
      </div>
      <div class="statistics-filter">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          range-separator="至"
        />
        <el-button type="primary" :loading="statisticsLoading" @click="loadStatistics">查询</el-button>
      </div>
    </div>

    <div class="stat-row">
      <div><strong>￥{{ money(orderStats.totalAmount) }}</strong><span>交易总金额</span></div>
      <div><strong>{{ orderStats.orderCount || 0 }}</strong><span>交易数</span></div>
      <div><strong>{{ orderStats.quantityTotal || 0 }}</strong><span>成交件数</span></div>
      <div><strong>￥{{ money(orderStats.averageOrderAmount) }}</strong><span>平均交易价</span></div>
    </div>

    <div class="statistics-grid">
      <div class="admin-workflow">
        <h2>按品类统计</h2>
        <div class="stat-list">
          <div v-for="item in orderStats.byCategory || []" :key="item.label" class="stat-list-item">
            <div>
              <strong>{{ item.label }}</strong>
              <span>{{ item.orderCount }} 单 / {{ item.quantity }} 件</span>
            </div>
            <em>￥{{ money(item.amount) }}</em>
            <i :style="{ width: barWidth(item.amount, categoryMax) }"></i>
          </div>
          <el-empty v-if="!(orderStats.byCategory || []).length" description="暂无品类统计" />
        </div>
      </div>

      <div class="admin-workflow">
        <h2>按日期统计</h2>
        <el-table :data="orderStats.byDate || []" max-height="360">
          <el-table-column prop="label" label="日期" min-width="110" />
          <el-table-column prop="orderCount" label="订单" width="80" />
          <el-table-column prop="quantity" label="件数" width="80" />
          <el-table-column label="金额" width="130">
            <template #default="{ row }">￥{{ money(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </section>
</template>

<!--
  文件说明：Dashboard.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '../../api/modules'

const stats = ref({})
const orderStats = ref({})
const loading = ref(false)
const statisticsLoading = ref(false)

function pad(value) {
  return String(value).padStart(2, '0')
}

function formatDate(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function defaultRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 29)
  return [formatDate(start), formatDate(end)]
}

const dateRange = ref(defaultRange())
const categoryMax = computed(() => Math.max(...(orderStats.value.byCategory || []).map(item => Number(item.amount || 0)), 0))

const money = (value) => Number(value || 0).toFixed(2)
const barWidth = (amount, max) => {
  if (!max) return '0%'
  return `${Math.max((Number(amount || 0) / max) * 100, 4)}%`
}

const load = async () => {
  loading.value = true
  try {
    stats.value = (await adminApi.dashboard()).data || {}
    await loadStatistics()
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  statisticsLoading.value = true
  try {
    const params = dateRange.value?.length === 2
      ? { startDate: dateRange.value[0], endDate: dateRange.value[1] }
      : {}
    orderStats.value = (await adminApi.orderStatistics(params)).data || {}
  } finally {
    statisticsLoading.value = false
  }
}

onMounted(load)
</script>
