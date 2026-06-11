<template>
  <section class="merchant-page">
    <div class="toolbar">
      <h1>商家订单管理</h1>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="orderNo" label="交易号" min-width="180" />
      <el-table-column prop="subOrderNo" label="子交易号" min-width="170" />
      <el-table-column prop="customerName" label="用户" width="120" />
      <el-table-column prop="productName" label="物品" min-width="160" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column label="金额" width="120"><template #default="{ row }">¥{{ row.totalAmount }}</template></el-table-column>
      <el-table-column label="状态" width="140"><template #default="{ row }">{{ statusLabel(row.status) }}</template></el-table-column>
      <el-table-column prop="contactPhone" label="电话" width="140" />
      <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button v-if="row.status === 'WAIT_SELLER_SEND_GOODS'" type="primary" @click="ship(row)">发货</el-button>
          <el-button v-if="row.status === 'TRADE_FINISHED' || row.status === 'AFTER_SALES_FINISHED'" type="success" @click="reviewBuyer(row)">评价买家</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { merchantApi } from '../../api/modules'

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
    list.value = (await merchantApi.orders({ page: 1, size: 50 })).data.content || []
  } finally {
    loading.value = false
  }
}

const ship = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入物流单号', '订单发货', { inputValue: row.logisticsNo || '' })
  await merchantApi.shipOrder(row.id, value)
  ElMessage.success('已发货，订单进入待收货')
  load()
}

const reviewBuyer = async (row) => {
  try {
    const { value: ratingStr } = await ElMessageBox.prompt('对买家评分（1-5）', '评价买家', { inputValue: '5' })
    const rating = parseInt(ratingStr)
    if (isNaN(rating) || rating < 1 || rating > 5) {
      ElMessage.error('评分需为1到5的整数')
      return
    }
    const { value: content } = await ElMessageBox.prompt('评价内容（可选）', '评价买家', { inputValue: '' })
    await merchantApi.reviewBuyer(row.id, rating, content)
    ElMessage.success('评价已提交')
    load()
  } catch (err) {
    // 用户取消或请求失败
  }
}

onMounted(load)
</script>

<style scoped>
.merchant-page .toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>

