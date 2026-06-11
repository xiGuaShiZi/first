<template>
  <section class="page-title">
    <p>Contact</p>
    <h1>交易咨询</h1>
    <span>闲置物品咨询、面交取件、交易价格和协商问题都可以在这里留言。</span>
  </section>
  <section class="section contact-grid">
    <div class="contact-info">
      <h2>{{ company.companyName || '校园二手街' }}</h2>
      <p><strong>电话</strong>{{ company.phone || '400-800-2026' }}</p>
      <p><strong>邮箱</strong>{{ company.email || 'campustrade@example.com' }}</p>
      <p><strong>地址</strong>{{ company.address || '校园交易服务中心' }}</p>
      <div class="map-block">校园自取、面交与交易协商服务覆盖校内场景</div>
    </div>
    <el-form class="message-form" :model="form" label-position="top">
      <el-form-item label="姓名"><el-input v-model="form.username" placeholder="请输入姓名" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" placeholder="请输入联系电话" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item>
      <el-form-item label="需求说明">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          maxlength="2000"
          show-word-limit
          placeholder="请简要描述闲置物品咨询、面交时间、交易价格或协商问题"
        />
      </el-form-item>
      <el-button type="primary" :loading="submitting" @click="submit">提交咨询</el-button>
    </el-form>
  </section>
</template>

<!--
  文件说明：Contact.vue
  作用：交易咨询页面，展示联系方式并收集用户留言。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { publicApi } from '../../api/modules'

const company = ref({})
const submitting = ref(false)
const form = reactive({ username: '', phone: '', email: '', content: '' })
const submit = async () => {
  if (!form.username.trim() || !form.content.trim()) {
    ElMessage.warning('请填写姓名和需求说明')
    return
  }
  submitting.value = true
  try {
    await publicApi.submitMessage(form)
    ElMessage.success('咨询已提交')
    Object.assign(form, { username: '', phone: '', email: '', content: '' })
  } finally {
    submitting.value = false
  }
}
onMounted(async () => {
  company.value = (await publicApi.company()).data || {}
})
</script>
