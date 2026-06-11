<template>
  <section class="page-title">
    <p>Wallet</p>
    <h1>我的钱包</h1>
    <span>查看钱包余额、交易流水和收入统计</span>
  </section>

  <section class="section">
    <!-- 钱包概览 -->
    <el-row :gutter="20" style="margin-bottom: 30px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="可用余额" :value="wallet.availableBalance || 0" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="冻结金额" :value="wallet.frozenBalance || 0" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="累计收入" :value="wallet.totalIncome || 0" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="累计支出" :value="wallet.totalExpense || 0" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 交易流水 -->
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <h3 style="margin:0;">交易流水</h3>
          <el-button @click="loadTransactions">刷新</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="transactions" style="width: 100%">
        <el-table-column prop="transactionType" label="交易类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getTransactionTypeTag(row.transactionType)">
              {{ getTransactionTypeText(row.transactionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.amount >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ row.amount >= 0 ? '+' : '' }}¥{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" width="120">
          <template #default="{ row }">
            ¥{{ row.balanceAfter.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > pageSize"
        layout="prev, pager, next"
        :current-page="page"
        :total="total"
        :page-size="pageSize"
        @current-change="changePage"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { merchantApi } from '../../api/modules'
import { ElMessage } from 'element-plus'

const wallet = ref({})
const transactions = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const loadWallet = async () => {
  try {
    const res = await merchantApi.getWallet()
    wallet.value = res.data || res || {}
  } catch (error) {
    ElMessage.error('加载钱包信息失败')
  }
}

const loadTransactions = async () => {
  loading.value = true
  try {
    const res = await merchantApi.getWalletTransactions({
      page: page.value,
      size: pageSize
    })
    const data = res.data || res || {}
    transactions.value = data.content || []
    total.value = data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载交易流水失败')
  } finally {
    loading.value = false
  }
}

const changePage = (value) => {
  page.value = value
  loadTransactions()
}

const getTransactionTypeText = (type) => {
  const map = {
    'ORDER_INCOME': '订单收入',
    'AUTO_CONFIRM': '自动确认',
    'WITHDRAW': '提现',
    'PLATFORM_FEE': '平台服务费',
    'REFUND': '退款'
  }
  return map[type] || type
}

const getTransactionTypeTag = (type) => {
  const map = {
    'ORDER_INCOME': 'success',
    'AUTO_CONFIRM': 'success',
    'WITHDRAW': 'danger',
    'PLATFORM_FEE': 'warning',
    'REFUND': 'info'
  }
  return map[type] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadWallet()
  loadTransactions()
})
</script>