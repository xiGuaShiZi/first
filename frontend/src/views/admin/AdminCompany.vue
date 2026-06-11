<template>
  <section class="admin-page narrow">
    <div class="admin-heading">
      <div>
        <h1>平台信息管理</h1>
        <p>维护校园二手交易平台的名称、简介、联系方式和 Logo。</p>
      </div>
    </div>
    <el-form :model="form" label-position="top">
      <el-form-item label="平台名称"><el-input v-model="form.companyName" /></el-form-item>
      <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" :rows="5" /></el-form-item>
      <el-form-item label="文化/优势"><el-input v-model="form.culture" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="官网"><el-input v-model="form.website" /></el-form-item>
      <el-form-item label="服务时间"><el-input v-model="form.serviceTime" /></el-form-item>
      <el-form-item label="Logo"><ImageUpload v-model="form.logo" /></el-form-item>
      <el-button type="primary" :loading="saving" @click="save">保存平台信息</el-button>
    </el-form>
  </section>
</template>

<!--
  文件说明：AdminCompany.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api/modules'
import ImageUpload from '../../components/ImageUpload.vue'

const saving = ref(false)
const form = reactive({ companyName: '', intro: '', culture: '', phone: '', email: '', address: '', logo: '', website: '', serviceTime: '' })
onMounted(async () => Object.assign(form, (await adminApi.company()).data || {}))
const save = async () => {
  if (!form.companyName || !form.intro) {
    ElMessage.warning('请填写平台名称和简介')
    return
  }
  saving.value = true
  try {
    await adminApi.updateCompany(form)
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}
</script>
