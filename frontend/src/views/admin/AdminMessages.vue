<template>
  <section class="admin-page">
    <div class="toolbar">
      <h1>咨询反馈</h1>
      <el-select v-model="status" clearable placeholder="状态筛选" @change="search">
        <el-option label="未处理" :value="0" />
        <el-option label="已处理" :value="1" />
      </el-select>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="username" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="email" label="邮箱" width="190" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">{{ row.status === 1 ? '已处理' : '未处理' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button @click="view(row)">查看</el-button>
          <el-button v-if="row.status !== 1" type="primary" @click="mark(row, 1)">处理</el-button>
          <el-button v-else @click="mark(row, 0)">重开</el-button>
          <el-button type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-if="total > pageSize"
      layout="prev, pager, next"
      :current-page="page"
      :total="total"
      :page-size="pageSize"
      @current-change="changePage"
    />

    <el-dialog v-model="visible" title="咨询详情" width="560px">
      <div class="message-detail">
        <p><strong>姓名</strong>{{ current.username }}</p>
        <p><strong>电话</strong>{{ current.phone || '-' }}</p>
        <p><strong>邮箱</strong>{{ current.email || '-' }}</p>
        <p><strong>时间</strong>{{ current.createTime?.slice(0, 19).replace('T', ' ') || '-' }}</p>
        <p><strong>内容</strong>{{ current.content }}</p>
      </div>
    </el-dialog>
  </section>
</template>

<!--
  文件说明：AdminMessages.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { inject, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/modules'

const refreshPendingCounts = inject('refreshPendingCounts')

const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const status = ref(null)
const loading = ref(false)
const visible = ref(false)
const current = ref({})

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize }
    if (status.value !== null && status.value !== '') params.status = status.value
    const res = await adminApi.messages(params)
    list.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}
const search = () => { page.value = 1; load() }
const changePage = (value) => { page.value = value; load() }
const view = (row) => {
  current.value = row
  visible.value = true
}
const mark = async (row, nextStatus) => {
  await adminApi.updateMessageStatus(row.id, nextStatus)
  ElMessage.success('状态已更新')
  refreshPendingCounts()
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该咨询？', '删除确认', { type: 'warning' })
  await adminApi.deleteMessage(id)
  ElMessage.success('已删除')
  refreshPendingCounts()
  load()
}
onMounted(load)
</script>
