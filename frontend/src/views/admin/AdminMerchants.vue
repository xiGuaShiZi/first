<template>
  <div class="admin-merchants">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商家审核管理</h2>
          <el-select v-model="auditStatus" placeholder="审核状态" @change="loadData" style="width: 200px">
            <el-option label="全部" :value="null" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </div>
      </template>
      
      <el-table :data="merchants" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="bankAccount" label="银行账号" width="180" />
        <el-table-column label="身份证" width="120">
          <template #default="{ row }">
            <el-image 
              v-if="row.idCardImage" 
              :src="row.idCardImage" 
              :preview-src-list="[row.idCardImage]"
              fit="cover"
              style="width: 60px; height: 60px; cursor: pointer"
            />
          </template>
        </el-table-column>
        <el-table-column label="营业执照" width="120">
          <template #default="{ row }">
            <el-image 
              v-if="row.businessLicense" 
              :src="row.businessLicense" 
              :preview-src-list="[row.businessLicense]"
              fit="cover"
              style="width: 60px; height: 60px; cursor: pointer"
            />
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="200" />
        <el-table-column prop="auditTime" label="审核时间" width="180" />
        <el-table-column prop="merchantLevel" label="商家等级" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.merchantLevel" disabled />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.auditStatus === 0" 
              type="primary" 
              size="small" 
              @click="showAuditDialog(row)"
            >
              审核
            </el-button>
            <el-button 
              v-if="row.auditStatus === 1" 
              type="warning" 
              size="small" 
              @click="showLevelDialog(row)"
            >
              设置等级
            </el-button>
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
    
    <el-dialog v-model="auditDialogVisible" title="审核商家" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="商家名称">
          {{ currentMerchant?.username }}
        </el-form-item>
        <el-form-item label="真实姓名">
          {{ currentMerchant?.realName }}
        </el-form-item>
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

    <el-dialog v-model="levelDialogVisible" title="设置商家等级" width="400px">
      <el-form :model="levelForm" label-width="100px">
        <el-form-item label="商家名称">
          {{ levelMerchant?.username }}
        </el-form-item>
        <el-form-item label="当前等级">
          {{ levelMerchant?.merchantLevel || 1 }} 级
        </el-form-item>
        <el-form-item label="设置等级">
          <el-rate v-model="levelForm.merchantLevel" :max="5" :min="1" show-text
            :texts="['1级 (0.1%)', '2级 (0.2%)', '3级 (0.5%)', '4级 (0.75%)', '5级 (1%)']" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLevel" :loading="levelSubmitting">保存</el-button>
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
const merchants = ref([])
const auditStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const auditDialogVisible = ref(false)
const currentMerchant = ref(null)
const submitting = ref(false)
const auditForm = reactive({
  auditStatus: 1,
  auditRemark: ''
})

const levelDialogVisible = ref(false)
const levelMerchant = ref(null)
const levelSubmitting = ref(false)
const levelForm = reactive({
  merchantLevel: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (auditStatus.value !== null) {
      params.auditStatus = auditStatus.value
    }
    const res = await adminApi.merchants(params)
    merchants.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

const showAuditDialog = (merchant) => {
  currentMerchant.value = merchant
  auditForm.auditStatus = 1
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.auditRemark) {
    ElMessage.warning('请输入审核备注')
    return
  }
  
  submitting.value = true
  try {
    await adminApi.auditMerchant(currentMerchant.value.id, auditForm)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    refreshPendingCounts()
    loadData()
  } finally {
    submitting.value = false
  }
}

const showLevelDialog = (merchant) => {
  levelMerchant.value = merchant
  levelForm.merchantLevel = merchant.merchantLevel || 1
  levelDialogVisible.value = true
}

const submitLevel = async () => {
  levelSubmitting.value = true
  try {
    await adminApi.setMerchantLevel(levelMerchant.value.id, { merchantLevel: levelForm.merchantLevel })
    ElMessage.success('等级设置成功')
    levelDialogVisible.value = false
    loadData()
  } finally {
    levelSubmitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.admin-merchants {
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