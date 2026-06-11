<template>
  <div class="image-upload">
    <el-input v-if="allowUrl" v-model="model" placeholder="图片地址，可手动填写或上传" />
    <el-input v-else :model-value="model" placeholder="请上传本地图片" readonly />
    <el-upload
      :auto-upload="false"
      :show-file-list="false"
      accept="image/*"
      :on-change="handleChange"
    >
      <el-button>选择图片</el-button>
    </el-upload>
    <img v-if="model" :src="imageUrl" alt="" />
  </div>
</template>

<!--
  文件说明：ImageUpload.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi, merchantApi, userApi } from '../api/modules'

const model = defineModel({ type: String, default: '' })
defineProps({
  allowUrl: { type: Boolean, default: true }
})

const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}

const imageUrl = computed(() => normalizeImageUrl(model.value))

const getUploadApi = () => {
  if (localStorage.getItem('merchant_token')) return merchantApi.upload
  if (localStorage.getItem('user_token')) return userApi.upload
  if (localStorage.getItem('admin_token')) return adminApi.upload
  return null
}

const handleChange = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }
  const uploadApi = getUploadApi()
  if (!uploadApi) {
    ElMessage.warning('请先登录后再上传图片')
    return
  }

  const res = await uploadApi(file)
  const payload = res?.data ?? res
  const uploadedUrl = payload?.url || payload?.fileUrl || payload?.imageUrl || payload?.path
  if (!uploadedUrl) {
    ElMessage.error('图片上传失败，请稍后重试')
    return
  }
  model.value = uploadedUrl
  ElMessage.success('图片上传成功')
}
</script>
