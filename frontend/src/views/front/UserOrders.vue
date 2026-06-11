<template>
  <section class="page-title">
    <p>Transactions</p>
    <h1>我的交易</h1>
    <span>同一次交易清单会生成一个交易单，便于确认收货、评价和后续协商。</span>
  </section>

  <section class="section">
    <el-alert v-if="!hasToken" title="请先登录会员账号" type="warning" show-icon :closable="false" />

    <template v-else>
      <div v-loading="loading" class="order-groups">
        <el-empty v-if="!loading && !orderGroups.length" description="暂无交易" />

        <article v-for="group in orderGroups" :key="group.mainOrderNo" class="order-group">
          <header class="order-group-head">
            <div>
              <p class="eyebrow">Order</p>
              <h2>{{ orderGroupTitle(group) }}</h2>
              <span>{{ group.items.length }} 件物品 · {{ statusLabel(group.status) }} · 交易单号 {{ group.mainOrderNo }}</span>
            </div>
            <div class="order-group-actions">
              <strong>¥{{ group.totalAmount.toFixed(2) }}</strong>
              <el-button v-if="group.status === 'WAIT_BUYER_PAY'" type="primary" @click="payGroup(group)">支付</el-button>
              <el-button v-if="group.status === 'WAIT_BUYER_PAY'" @click="cancelGroup(group)">取消</el-button>
            </div>
          </header>

          <el-table :data="group.items">
            <el-table-column prop="orderNo" label="交易编号" min-width="170" />
            <el-table-column prop="productName" label="物品" min-width="160" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column label="状态" width="130">
              <template #default="{ row }">{{ statusLabel(row.status) }}</template>
            </el-table-column>
            <el-table-column label="评价" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.isReviewed === 1" type="success">已评价</el-tag>
                <el-button v-else-if="row.status === 'TRADE_FINISHED'" type="primary" @click="goReview(row)">评价</el-button>
                <span v-else class="muted-text">完成后可评</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="190">
              <template #default="{ row }">
                <el-button v-if="canConfirm(row)" type="success" @click="confirmReceipt(row)">确认收货</el-button>
                <el-button v-if="canApplyReturn(row)" @click="openReturn(row)">售后协商</el-button>
              </template>
            </el-table-column>
          </el-table>
        </article>
      </div>
    </template>

    <div class="section-head order-return-head">
      <div>
        <p class="eyebrow">After Sales</p>
        <h2>交易协商记录</h2>
      </div>
    </div>
    <el-table v-if="hasToken" :data="returns">
      <el-table-column prop="orderNo" label="交易号" min-width="180" />
      <el-table-column prop="productName" label="物品" min-width="150" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ typeLabel(row.requestType) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">{{ returnStatusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="reply" label="客服回复" />
    </el-table>

    <el-dialog v-model="visible" title="发起交易协商" width="560px">
      <el-form label-position="top">
        <el-form-item label="协商类型">
          <el-select v-model="requestType">
            <el-option label="仅退款" value="ONLY_REFUND" />
            <el-option label="退货退款" value="RETURN_REFUND" />
            <el-option label="换货" value="EXCHANGE" />
            <el-option label="维修" value="REPAIR" />
          </el-select>
        </el-form-item>
        <el-form-item label="协商原因">
          <el-input v-model="reason" type="textarea" :rows="5" placeholder="请说明退款、退货、换货或维修诉求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="submitReturn">提交</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<!--
  文件说明：UserOrders.vue
  作用：我的交易页面，展示订单列表、状态、支付和售后入口。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api/modules'

const router = useRouter()
const orders = ref([])
const returns = ref([])
const loading = ref(false)
const visible = ref(false)
const currentOrder = ref(null)
const requestType = ref('ONLY_REFUND')
const reason = ref('')
const hasToken = computed(() => Boolean(localStorage.getItem('user_token')))

const returnTypeMap = { ONLY_REFUND: '仅退款', RETURN_REFUND: '退货退款', EXCHANGE: '换货', REPAIR: '维修' }
const orderStatusMap = {
  WAIT_BUYER_PAY: '待付款',
  WAIT_SELLER_SEND_GOODS: '待发货',
  WAIT_BUYER_CONFIRM_GOODS: '待收货',
  TRADE_FINISHED: '交易成功',
  TRADE_CLOSED: '已关闭',
  REFUNDING: '退款中',
  RETURNING: '退货中',
  AFTER_SALES_FINISHED: '售后完成'
}
const returnStatusMap = {
  APPLY_PENDING: '申请待审核',
  APPROVED: '审核通过',
  REJECTED: '审核拒绝',
  BUYER_RETURNING: '买家寄回',
  SELLER_RECEIVED: '卖家验收',
  REFUNDED: '已退款',
  RESHIPPED: '换新发货',
  COMPLETED: '完成',
  CLOSED: '关闭'
}

const orderGroups = computed(() => {
  const map = new Map()
  for (const order of orders.value) {
    const key = order.mainOrderNo || order.orderNo
    if (!map.has(key)) {
      map.set(key, { mainOrderNo: key, items: [], totalAmount: 0, status: order.status, createTime: order.createTime })
    }
    const group = map.get(key)
    group.items.push(order)
    group.totalAmount += Number(order.totalAmount || 0)
    group.status = groupStatus(group.items)
  }
  return [...map.values()].sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
})

const groupStatus = (items) => {
  if (items.some(item => item.status === 'WAIT_BUYER_PAY')) return 'WAIT_BUYER_PAY'
  if (items.some(item => item.status === 'WAIT_SELLER_SEND_GOODS')) return 'WAIT_SELLER_SEND_GOODS'
  if (items.some(item => item.status === 'WAIT_BUYER_CONFIRM_GOODS')) return 'WAIT_BUYER_CONFIRM_GOODS'
  if (items.every(item => item.status === 'TRADE_FINISHED')) return 'TRADE_FINISHED'
  if (items.every(item => item.status === 'TRADE_CLOSED')) return 'TRADE_CLOSED'
  return items[0]?.status || ''
}

const typeLabel = (value) => returnTypeMap[value] || value
const statusLabel = (value) => orderStatusMap[value] || value
const returnStatusLabel = (value) => returnStatusMap[value] || value
const orderGroupTitle = (group) => `${String(group.createTime || '').slice(0, 10) || '交易'}-交易包裹`

const load = async () => {
  if (!hasToken.value) return
  loading.value = true
  try {
    orders.value = (await userApi.orders({ page: 1, size: 100 })).data.content || []
    returns.value = (await userApi.returns({ page: 1, size: 100 })).data.content || []
  } finally {
    loading.value = false
  }
}

const canConfirm = (row) => row.status === 'WAIT_BUYER_CONFIRM_GOODS'
const canApplyReturn = (row) => ['WAIT_SELLER_SEND_GOODS', 'WAIT_BUYER_CONFIRM_GOODS', 'TRADE_FINISHED'].includes(row.status)

const payGroup = async (group) => {
  const firstPayable = group.items.find(item => item.status === 'WAIT_BUYER_PAY')
  if (!firstPayable) return
  await ElMessageBox.confirm(`将一次支付主订单 ${group.mainOrderNo} 下的 ${group.items.length} 件物品，确认继续？`, '支付订单', { type: 'warning' })
  await userApi.payOrder(firstPayable.id)
  ElMessage.success('支付成功，该交易单下物品已进入后续处理')
  load()
}

const cancelGroup = async (group) => {
  const payableItems = group.items.filter(item => item.status === 'WAIT_BUYER_PAY')
  if (!payableItems.length) return
  await ElMessageBox.confirm(`确认取消交易单 ${group.mainOrderNo} 下所有待付款物品？`, '取消交易', { type: 'warning' })
  await userApi.cancelOrder(payableItems[0].id)
  ElMessage.success('交易单已关闭')
  load()
}

const confirmReceipt = async (row) => {
  await ElMessageBox.confirm('确认已完成该交易？确认后即可评价。', '确认收货', { type: 'warning' })
  await userApi.confirmReceipt(row.id)
  ElMessage.success('已确认收货')
  load()
}

const goReview = (row) => {
  router.push(`/orders/${row.id}/review`)
}

const openReturn = (row) => {
  currentOrder.value = row
  requestType.value = row.status === 'WAIT_SELLER_SEND_GOODS' ? 'ONLY_REFUND' : 'RETURN_REFUND'
  reason.value = ''
  visible.value = true
}

const submitReturn = async () => {
  if (!reason.value) {
    ElMessage.warning('请填写协商原因')
    return
  }
  await userApi.createReturn({ orderId: currentOrder.value.id, requestType: requestType.value, reason: reason.value })
  ElMessage.success('已提交售后协商')
  visible.value = false
  load()
}

onMounted(load)
</script>
