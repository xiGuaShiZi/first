<template>
  <section class="admin-page">
    <div class="toolbar">
      <h1>交易协商</h1>
      <el-select v-model="status" clearable placeholder="状态筛选" @change="load">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="orderNo" label="交易号" min-width="170" />
      <el-table-column prop="customerName" label="用户" width="120" />
      <el-table-column prop="productName" label="物品" min-width="150" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">{{ typeLabel(row.requestType) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" show-overflow-tooltip />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">{{ statusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button @click="open(row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="处理交易协商" width="560px">
      <el-form label-position="top">
        <el-form-item label="处理结果">
          <el-select v-model="form.status">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="客服回复"><el-input v-model="form.reply" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<!--
  文件说明：AdminReturns.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { inject, reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api/modules'

const refreshPendingCounts = inject('refreshPendingCounts')

const list = ref([])
const status = ref('')
const loading = ref(false)
const visible = ref(false)
const currentId = ref(null)
const form = reactive({ status: 'APPLY_PENDING', reply: '' })
const statusOptions = [
  { label: '申请待审核', value: 'APPLY_PENDING' },
  { label: '审核通过', value: 'APPROVED' },
  { label: '审核拒绝', value: 'REJECTED' },
  { label: '买家寄回', value: 'BUYER_RETURNING' },
  { label: '卖家收货验收', value: 'SELLER_RECEIVED' },
  { label: '已退款', value: 'REFUNDED' },
  { label: '换新发货', value: 'RESHIPPED' },
  { label: '完成', value: 'COMPLETED' },
  { label: '关闭', value: 'CLOSED' }
]
const returnTypeMap = { ONLY_REFUND: '仅退款', RETURN_REFUND: '退货退款', EXCHANGE: '换货', REPAIR: '维修' }
const typeLabel = (value) => returnTypeMap[value] || value
const statusLabel = (value) => statusOptions.find(item => item.value === value)?.label || value

const load = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (status.value) params.status = status.value
    list.value = (await adminApi.returns(params)).data.content || []
  } finally {
    loading.value = false
  }
}
const open = (row) => {
  currentId.value = row.id
  form.status = row.status
  form.reply = row.reply || ''
  visible.value = true
}
const save = async () => {
  await adminApi.handleReturn(currentId.value, form)
  ElMessage.success('售后状态已更新')
  visible.value = false
  refreshPendingCounts()
  load()
}
onMounted(load)
</script>
