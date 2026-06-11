<template>
  <div class="admin-shell">
    <aside>
      <div class="admin-brand">校园二手后台</div>
      <div class="admin-account">
        <strong>{{ adminName }}</strong>
        <span>管理员</span>
      </div>
      <router-link to="/admin">概览</router-link>
      <router-link to="/admin/news">校园贴士</router-link>
      <router-link to="/admin/product">
        闲置物品
        <span v-if="pendingCounts.productPendingAudit" class="badge badge-red">{{ pendingCounts.productPendingAudit }}</span>
      </router-link>
      <router-link to="/admin/message">
        咨询反馈
        <span v-if="pendingCounts.pendingMessageTotal" class="badge badge-orange">{{ pendingCounts.pendingMessageTotal }}</span>
      </router-link>
      <router-link to="/admin/company">平台信息</router-link>
      <router-link to="/admin/banner">Banner</router-link>
      <router-link to="/admin/orders">交易管理</router-link>
      <router-link to="/admin/returns">
        交易协商
        <span v-if="pendingCounts.returnPendingTotal" class="badge badge-orange">{{ pendingCounts.returnPendingTotal }}</span>
      </router-link>
      <router-link to="/admin/merchants">
        商家审核
        <span v-if="pendingCounts.merchantPendingTotal" class="badge badge-red">{{ pendingCounts.merchantPendingTotal }}</span>
      </router-link>
      <router-link to="/admin/customers">
        客户审核
        <span v-if="pendingCounts.customerPendingTotal" class="badge badge-red">{{ pendingCounts.customerPendingTotal }}</span>
      </router-link>
      <router-link to="/admin/service-fees">服务费管理</router-link>
      <el-button type="text" @click="adjustMerchantLevels" style="color:#409EFF; padding:0; margin-top:8px">手动调整商家等级</el-button>
      <button @click="goToFront">返回前台</button>
      <button @click="passwordDialogVisible = true">修改密码</button>
      <button @click="logout">退出登录</button>
    </aside>
    <main>
      <router-view />
    </main>

    <el-dialog v-model="passwordDialogVisible" title="修改管理员密码" width="420px">
      <el-form label-width="82px">
        <el-form-item label="原密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="changePassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<!--
  文件说明：AdminLayout.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, onUnmounted, provide, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi, authApi } from '../api/modules'

const router = useRouter()
const adminName = computed(() => localStorage.getItem('admin_name') || 'admin')
const passwordDialogVisible = ref(false)
const saving = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pendingCounts = ref({})
let pollTimer = null

const goToFront = () => {
  router.push('/')
}

const logout = async () => {
  await authApi.logout().catch(() => {})
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_name')
  window.dispatchEvent(new Event('admin-auth-changed'))
  router.push('/user-login')
}

const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请输入原密码和新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  saving.value = true
  try {
    await adminApi.changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    passwordDialogVisible.value = false
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    ElMessage.success('密码修改成功')
  } finally {
    saving.value = false
  }
}

const fetchPendingCounts = async () => {
  try {
    const res = await adminApi.dashboard()
    pendingCounts.value = res.data || {}
  } catch {
    // 静默失败，下次轮询重试
  }
}

provide('refreshPendingCounts', fetchPendingCounts)

onMounted(() => {
  fetchPendingCounts()
  pollTimer = setInterval(fetchPendingCounts, 30000)
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})

const adjustMerchantLevels = async () => {
  try {
    const res = await adminApi.adjustMerchantLevels()
    const changed = res.data || {}
    const count = Object.keys(changed).length
    if (count > 0) {
      ElMessage.success(`商家等级已调整，变更 ${count} 个商家`)
    } else {
      ElMessage.info('未发现需要调整的商家等级')
    }
  } catch (err) {
    ElMessage.error('触发商家等级调整失败')
  }
}
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  margin-left: 6px;
}
.badge-red {
  background: #F56C6C;
}
.badge-orange {
  background: #E6A23C;
}
</style>
