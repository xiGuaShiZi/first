<template>
  <div class="admin-merchant-level-audits">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商家等级变更审计</h2>
        </div>
      </template>

      <el-table :data="audits" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="merchantId" label="商家ID" width="120" />
        <el-table-column prop="operatorName" label="操作人" width="140" />
        <el-table-column prop="oldLevel" label="旧等级" width="100">
          <template #default="{ row }">{{ row.oldLevel }} 级</template>
        </el-table-column>
        <el-table-column prop="newLevel" label="新等级" width="100">
          <template #default="{ row }">{{ row.newLevel }} 级</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="180" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        @current-change="loadData"
        @size-change="loadData"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api/modules'

const loading = ref(false)
const audits = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.merchantLevelAudits({ page: currentPage.value, size: pageSize.value })
    audits.value = res.data.content
    total.value = res.data.totalElements
  } catch (err) {
    ElMessage.error('加载审计记录失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.admin-merchant-level-audits {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}
</style>

