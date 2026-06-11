<template>
  <div class="buyer-reviews">
    <h2>商家对我的评价</h2>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="merchantName" label="商家" width="160" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column prop="content" label="评价内容" />
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <div class="pager" style="margin-top:12px; text-align:right">
        <el-pagination
          background
          :page-size="pageSize"
          :current-page.sync="page"
          :total="total"
          layout="prev, pager, next"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../../api/modules'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

const load = async (p = page.value) => {
  loading.value = true
  try {
    const res = await userApi.buyerReviews({ page: p, size: pageSize })
    const data = res.data || res
    list.value = data.content || []
    total.value = data.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.buyer-reviews { padding: 16px }
</style>

