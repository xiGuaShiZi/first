<template>
  <section class="shop-settings">
    <div class="toolbar">
      <h1>店铺设置</h1>
    </div>

    <el-card v-loading="loading">
      <el-form label-width="100px" :model="form" class="settings-form">
        <el-form-item label="店铺名称">
          <el-input v-model="form.shopName" maxlength="100" show-word-limit placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="店铺描述">
          <el-input v-model="form.shopDescription" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入店铺描述" />
        </el-form-item>
        <el-form-item label="店铺Logo">
          <div class="logo-area">
            <el-avatar v-if="logoUrl" :size="80" :src="logoUrl" shape="square" />
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="uploadLogo"
              accept="image/*"
            >
              <el-button size="small">{{ logoUrl ? '更换Logo' : '上传Logo' }}</el-button>
            </el-upload>
            <el-button v-if="logoUrl" size="small" type="danger" plain @click="form.shopLogo = ''">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { merchantApi } from '../../api/modules'

const form = ref({ shopName: '', shopDescription: '', shopLogo: '' })
const loading = ref(false)
const saving = ref(false)

const API_ORIGIN = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'

const logoUrl = computed(() => {
  const v = form.value.shopLogo
  if (!v) return ''
  if (v.startsWith('http')) return v
  return `${API_ORIGIN.replace(/\/+$/, '')}/${v.replace(/^\/+/, '')}`
})

const load = async () => {
  loading.value = true
  try {
    const res = await merchantApi.getShopInfo()
    const data = res.data || res
    form.value = { shopName: data.shopName || '', shopDescription: data.shopDescription || '', shopLogo: data.shopLogo || '' }
  } catch {
    ElMessage.error('加载店铺信息失败')
  } finally {
    loading.value = false
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) ElMessage.error('请上传图片文件')
  return isImage
}

const uploadLogo = async ({ file }) => {
  try {
    const res = await merchantApi.upload(file)
    const url = res.data || res
    form.value.shopLogo = typeof url === 'string' ? url : url.url || ''
    ElMessage.success('Logo已上传')
  } catch {
    ElMessage.error('上传失败')
  }
}

const save = async () => {
  saving.value = true
  try {
    await merchantApi.updateShopInfo(form.value)
    ElMessage.success('店铺设置已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.shop-settings {
  padding: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.toolbar h1 {
  margin: 0;
  font-size: 20px;
}

.settings-form {
  max-width: 560px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>