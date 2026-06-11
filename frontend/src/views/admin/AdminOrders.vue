<template>
  <section class="admin-page">
    <div class="toolbar">
      <h1>交易管理</h1>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="orderNo" label="交易号" min-width="180" />
      <el-table-column prop="subOrderNo" label="子交易号" min-width="170" />
      <el-table-column prop="shopName" label="店铺" width="120" />
      <el-table-column prop="customerName" label="用户" width="120" />
      <el-table-column prop="productName" label="物品" min-width="160" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column label="金额" width="120"><template #default="{ row }">¥{{ row.totalAmount }}</template></el-table-column>
      <el-table-column label="状态" width="140"><template #default="{ row }">{{ statusLabel(row.status) }}</template></el-table-column>
      <el-table-column prop="contactPhone" label="电话" width="140" />
      <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-button v-if="row.status === 'WAIT_SELLER_SEND_GOODS'" type="primary" @click="ship(row)">发货</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<!--
  文件说明：AdminOrders.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { adminApi } from '../../api/modules'

const list = ref([])
const loading = ref(false)
const statusMap = {
  WAIT_BUYER_PAY: '待付款',
  WAIT_SELLER_SEND_GOODS: '待发货',
  WAIT_BUYER_CONFIRM_GOODS: '待收货',
  TRADE_FINISHED: '交易成功',
  TRADE_CLOSED: '已关闭',
  REFUNDING: '退款中',
  RETURNING: '退货中',
  AFTER_SALES_FINISHED: '售后完成'
}
const statusLabel = (value) => statusMap[value] || value

const load = async () => {
  loading.value = true
  try {
    list.value = (await adminApi.orders({ page: 1, size: 50 })).data.content || []
  } finally {
    loading.value = false
  }
}

const ship = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入物流单号', '订单发货', { inputValue: row.logisticsNo || '' })
  await adminApi.shipOrder(row.id, value)
  ElMessage.success('已发货，交易进入待收货')
  load()
}

onMounted(load)
</script>
