<template>
  <div class="admin-customers">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>客户审核管理</h2>
          <el-select v-model="auditStatus" placeholder="审核状态" @change="loadData" style="width: 200px">
            <el-option label="全部" :value="null" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </div>
      </template>

      <el-table :data="customers" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            ¥{{ (row.balance || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="auditStatus" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="200" />
        <el-table-column prop="auditTime" label="审核时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 0" type="primary" size="small" @click="showAuditDialog(row)">审核</el-button>
          </template>
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

    <el-dialog v-model="auditDialogVisible" title="审核客户" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="用户名">{{ currentCustomer?.username }}</el-form-item>
        <el-form-item label="手机号">{{ currentCustomer?.phone }}</el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { inject, ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api/modules'

const refreshPendingCounts = inject('refreshPendingCounts')

const loading = ref(false)
const customers = ref([])
const auditStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const auditDialogVisible = ref(false)
const currentCustomer = ref(null)
const submitting = ref(false)
const auditForm = reactive({ auditStatus: 1, auditRemark: '' })

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (auditStatus.value !== null) params.auditStatus = auditStatus.value
    const res = await adminApi.customers(params)
    customers.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

const showAuditDialog = (customer) => {
  currentCustomer.value = customer
  auditForm.auditStatus = 1
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.auditRemark) { ElMessage.warning('请输入审核备注'); return }
  submitting.value = true
  try {
    await adminApi.auditCustomer(currentCustomer.value.id, auditForm)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    refreshPendingCounts()
    loadData()
  } finally { submitting.value = false }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.admin-customers { padding: 20px }
.card-header { display:flex; justify-content:space-between; align-items:center }
.card-header h2{ margin:0; font-size:20px }
</style>

