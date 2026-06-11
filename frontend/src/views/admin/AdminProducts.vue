<template>
  <section class="admin-page">
    <div class="toolbar">
      <h1>闲置物品</h1>
      <el-button type="primary" @click="open()">新增物品</el-button>
    </div>
    <div class="admin-filter">
      <el-input v-model="keyword" placeholder="按闲置物品名称搜索" clearable @clear="search" @keyup.enter="search" />
      <el-button @click="search">搜索</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="auditStatus" label="审核状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.auditStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="封面" width="110">
        <template #default="{ row }"><img v-if="row.image" class="table-thumb" :src="imageSrc(row.image)" alt="" /></template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="sku" label="SKU" width="130" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="salesCount" label="销量" width="90" />
      <el-table-column prop="tags" label="标签" width="160" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '发布' : '下线' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button @click="open(row)">编辑</el-button>
          <el-button @click="toggle(row)">{{ row.status === 1 ? '下线' : '发布' }}</el-button>
          <el-button v-if="row.auditStatus === 0" type="primary" size="mini" @click="showAuditDialog(row)">审核</el-button>
          <el-button type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="闲置物品信息" width="760px">
        <el-form :model="form" label-position="top">
        <el-form-item label="SKU"><el-input v-model="form.sku" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" placeholder="件、台、套" /></el-form-item>
        <el-form-item label="销量"><el-input-number v-model="form.salesCount" :min="0" /></el-form-item>
        <el-form-item label="重量（克）"><el-input-number v-model="form.weightGrams" :min="0" /></el-form-item>
        <el-form-item label="封面图片">
          <ImageUpload v-model="form.image" :allow-url="false" />
          <span class="form-hint">封面图片必须从本地上传，保存后使用后端服务器返回的 /uploads/ 图片地址。</span>
        </el-form-item>
        <el-form-item label="物品详情图片">
          <div class="gallery-editor">
            <div v-for="(item, index) in form.images" :key="index" class="gallery-editor-item">
              <ImageUpload v-model="item.imageUrl" :allow-url="false" />
              <el-input v-model="item.caption" placeholder="图片说明，可选" />
              <el-input-number v-model="item.sort" :min="0" />
              <el-button type="danger" @click="removeGalleryImage(index)">删除</el-button>
            </div>
            <el-button @click="addGalleryImage">添加详情图片</el-button>
          </div>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="下线" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="物品详情"><el-input v-model="form.detail" type="textarea" :rows="8" placeholder="支持 HTML 图文详情，例如 <h2>物品卖点</h2><p>详细介绍</p>" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" title="审核商品" width="480px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="商品名称">{{ currentProduct?.name }}</el-form-item>
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
        <el-button @click="auditDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitProductAudit" :loading="auditSubmitting">提交</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<!--
  文件说明：AdminProducts.vue
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
const total = ref(0)
const keyword = ref('')
const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const pageSize = 10
const editingId = ref(null)
const emptyForm = { sku: '', name: '', category: '', tags: '', image: '', images: [], price: 0, originalPrice: 0, stock: 0, unit: '件', salesCount: 0, weightGrams: 0, description: '', detail: '', status: 1 }
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
    const res = await adminApi.products({ keyword: keyword.value, page: page.value, size: pageSize })
    list.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}
const search = () => { page.value = 1; load() }
const open = (row) => {
  editingId.value = row?.id || null
  Object.assign(form, emptyForm, row || {}, {
    images: normalizeImages(row?.images)
  })
  visible.value = true
}
const normalizeImages = (images) => Array.isArray(images)
  ? images.map((item, index) => ({
      imageUrl: item.imageUrl || '',
      caption: item.caption || '',
      sort: item.sort ?? index
    }))
  : []
const addGalleryImage = () => {
  form.images.push({ imageUrl: '', caption: '', sort: form.images.length })
}
const removeGalleryImage = (index) => {
  form.images.splice(index, 1)
}
const productPayload = () => ({
  ...form,
  images: form.images
    .filter(item => item.imageUrl)
    .map((item, index) => ({ ...item, sort: item.sort ?? index }))
})
const save = async () => {
  if (!form.name || !form.description) {
    ElMessage.warning('请填写名称和简介')
    return
  }
  saving.value = true
  try {
    const data = productPayload()
    editingId.value ? await adminApi.updateProduct(editingId.value, data) : await adminApi.createProduct(data)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const toggle = async (row) => {
  await adminApi.updateProduct(row.id, {
    sku: row.sku,
    name: row.name,
    category: row.category,
    tags: row.tags,
    image: row.image,
    images: normalizeImages(row.images),
    price: row.price,
    originalPrice: row.originalPrice,
    stock: row.stock,
    unit: row.unit,
    salesCount: row.salesCount,
    weightGrams: row.weightGrams,
    description: row.description,
    detail: row.detail,
    status: row.status === 1 ? 0 : 1
  })
  ElMessage.success('状态已更新')
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该闲置物品？', '删除确认', { type: 'warning' })
  await adminApi.deleteProduct(id)
  ElMessage.success('已删除')
  load()
}

/* ---- 审核相关 ---- */
const auditDialogVisible = ref(false)
const currentProduct = ref(null)
const auditForm = reactive({ auditStatus: 1, auditRemark: '' })
const auditSubmitting = ref(false)

const showAuditDialog = (row) => {
  currentProduct.value = row
  auditForm.auditStatus = 1
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const submitProductAudit = async () => {
  auditSubmitting.value = true
  try {
    await adminApi.auditProduct(currentProduct.value.id, {
      auditStatus: auditForm.auditStatus,
      auditRemark: auditForm.auditRemark
    })
    ElMessage.success(auditForm.auditStatus === 1 ? '审核通过' : '已拒绝')
    auditDialogVisible.value = false
    load()
  } finally {
    auditSubmitting.value = false
  }
}

onMounted(load)
</script>
