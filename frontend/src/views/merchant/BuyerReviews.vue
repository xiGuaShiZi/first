<template>
  <section class="merchant-page">
    <div class="toolbar">
      <h1>我对买家的评价</h1>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="暂无评价记录" />

    <div v-else>
      <div class="review-list" v-loading="loading">
        <div class="review-card" v-for="item in list" :key="item.id">
          <div class="review-header">
            <span class="buyer-name">买家：{{ item.buyerName || '—' }}</span>
            <el-rate :model-value="item.rating" disabled />
          </div>
          <div class="review-meta">
            <span>订单号：{{ item.orderNo || '—' }}</span>
            <span>评价时间：{{ formatTime(item.createTime) }}</span>
          </div>
          <div class="review-content">{{ item.content || '（无评价内容）' }}</div>
          <div class="review-type">
            <el-tag size="small" type="info">{{ reviewTypeLabel(item.reviewType) }}</el-tag>
          </div>
        </div>
      </div>

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
import { ElMessage } from 'element-plus'
import { merchantApi } from '../../api/modules'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const load = async (p = page.value) => {
  loading.value = true
  try {
    const res = await merchantApi.myBuyerReviews({ page: p, size: pageSize })
    const data = res.data || res
    list.value = data.content || []
    total.value = data.totalElements || 0
    page.value = p
  } catch (e) {
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (p) => load(p)

const formatTime = (t) => {
  if (!t) return '—'
  return t.replace('T', ' ').substring(0, 16)
}

const reviewTypeMap = { PURCHASE: '购买评价', RETURN: '退货评价' }
const reviewTypeLabel = (v) => reviewTypeMap[v] || v || '评价'

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

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.buyer-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.review-meta {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.review-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-type {
  display: flex;
  justify-content: flex-end;
}

.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
