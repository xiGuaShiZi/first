<template>
  <div class="bargain-offers">
    <h2>我的议价记录</h2>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="productName" label="商品" min-width="160" show-overflow-tooltip />
        <el-table-column label="原价" width="100">
          <template #default="{ row }">¥{{ row.originalPrice || '—' }}</template>
        </el-table-column>
        <el-table-column label="我的出价" width="100">
          <template #default="{ row }"><strong style="color: #e6a23c;">¥{{ row.offerPrice }}</strong></template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="商家回复" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reply || '—' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="danger" plain @click="cancelOffer(row)">取消</el-button>
            <el-button v-if="row.status === 'ACCEPTED'" size="small" type="success" @click="goBuy(row)">去购买</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager" style="margin-top:12px; text-align:right">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api/modules'

const router = useRouter()
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
    const res = await userApi.myBargainOffers({ page: p, size: pageSize })
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

const cancelOffer = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消这条议价记录吗？', '取消议价')
    await userApi.cancelBargainOffer(row.id)
    ElMessage.success('已取消')
    load()
  } catch { /* cancelled */ }
}

const goBuy = (row) => {
  router.push(`/product/${row.productId}?bargainOfferId=${row.id}&bargainPrice=${row.offerPrice}`)
}

onMounted(() => load())
</script>

<style scoped>
.bargain-offers { padding: 16px }
</style>
