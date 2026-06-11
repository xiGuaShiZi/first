<template>
  <section class="admin-page">
    <div class="toolbar">
      <h1>Banner 管理</h1>
      <el-button type="primary" @click="open()">新增 Banner</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="图片" width="120">
        <template #default="{ row }"><img v-if="row.image" class="table-thumb wide" :src="imageSrc(row.image)" alt="" /></template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="subtitle" label="副标题" min-width="180" />
      <el-table-column prop="link" label="链接" min-width="120" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button @click="open(row)">编辑</el-button>
          <el-button @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="Banner 信息" width="720px">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.link" placeholder="/product 或完整 URL" /></el-form-item>
        <el-form-item label="图片"><ImageUpload v-model="form.image" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<!--
  文件说明：AdminBanners.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/modules'
import ImageUpload from '../../components/ImageUpload.vue'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const editingId = ref(null)
const emptyForm = { title: '', subtitle: '', link: '', image: '', sort: 0, status: 1 }
const form = reactive({ ...emptyForm })
const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}
const imageSrc = (value) => normalizeImageUrl(value)

const load = async () => {
  loading.value = true
  try {
    list.value = (await adminApi.banners()).data || []
  } finally {
    loading.value = false
  }
}
const open = (row) => {
  editingId.value = row?.id || null
  Object.assign(form, emptyForm, row || {})
  visible.value = true
}
const save = async () => {
  if (!form.title || !form.image) {
    ElMessage.warning('请填写标题并上传图片')
    return
  }
  saving.value = true
  try {
    editingId.value ? await adminApi.updateBanner(editingId.value, form) : await adminApi.createBanner(form)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const toggle = async (row) => {
  await adminApi.updateBanner(row.id, { ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success('状态已更新')
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该 Banner？', '删除确认', { type: 'warning' })
  await adminApi.deleteBanner(id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>
