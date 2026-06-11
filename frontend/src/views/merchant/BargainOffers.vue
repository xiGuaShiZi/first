<template>
  <section class="merchant-page">
    <div class="toolbar">
      <h1>议价管理</h1>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="暂无议价记录" />

    <div v-else>
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="productName" label="商品" min-width="160" show-overflow-tooltip />
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column label="原价" width="100">
          <template #default="{ row }">¥{{ row.originalPrice || '—' }}</template>
        </el-table-column>
        <el-table-column label="买家出价" width="110">
          <template #default="{ row }"><strong style="color: #e6a23c;">¥{{ row.offerPrice }}</strong></template>
        </el-table-column>
        <el-table-column label="折扣" width="90">
          <template #default="{ row }">
            <span v-if="row.originalPrice">{{ Math.round((row.offerPrice / row.originalPrice) * 100) }}%</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reply || '—' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" @click="handleOffer(row, 'ACCEPT')">接受</el-button>
              <el-button size="small" type="danger" plain @click="handleOffer(row, 'REJECT')">拒绝</el-button>
            </template>
            <span v-else class="muted-text">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          :page-size="pageSize"
          :current-page="page"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { merchantApi } from '../../api/modules'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const statusMap = { PENDING: '待处理', ACCEPTED: '已接受', REJECTED: '已拒绝', CANCELLED: '已取消', USED: '已购买' }
const statusTypeMap = { PENDING: 'warning', ACCEPTED: 'success', REJECTED: 'danger', CANCELLED: 'info', USED: '' }
const statusLabel = (v) => statusMap[v] || v
const statusType = (v) => statusTypeMap[v] || 'info'

const formatTime = (t) => {
  if (!t) return '—'
  return String(t).replace('T', ' ').substring(0, 16)
}

const load = async (p = page.value) => {
  loading.value = true
  try {
    const res = await merchantApi.bargainOffers({ page: p, size: pageSize })
    const data = res.data || res
    list.value = data.content || []
    total.value = data.totalElements || 0
    page.value = p
  } catch {
    ElMessage.error('加载议价记录失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (p) => load(p)

const handleOffer = async (row, action) => {
  const actionText = action === 'ACCEPT' ? '接受' : '拒绝'
  try {
    const { value: reply } = await ElMessageBox.prompt(
      `${actionText}该议价（可填写回复，选填）`,
      `${actionText}议价`,
      { confirmButtonText: actionText, cancelButtonText: '取消' }
    )
    await merchantApi.handleBargainOffer(row.id, action, reply || '')
    ElMessage.success(`已${actionText}`)
    load()
  } catch { /* cancelled */ }
}

onMounted(() => load())
</script>

<style scoped>
.merchant-page {
  padding: 16px;
}

.merchant-page .toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pager {
  margin-top: 16px;
  text-align: right;
}

.muted-text {
  color: #909399;
  font-size: 13px;
}
</style>
