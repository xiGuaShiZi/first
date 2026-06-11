<template>
  <section class="page-title">
    <p>My Listings</p>
    <h1>我发布的闲置物品</h1>
    <span>在这里管理你发布的闲置物品，可以新增、编辑或删除。</span>
  </section>
  <section class="section">
    <div class="toolbar">
      <h2>我的物品列表</h2>
      <el-tag type="warning">普通用户已不支持发布物品，请使用商家账号或管理员接口进行上架</el-tag>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="封面" width="110">
        <template #default="{ row }"><img v-if="row.image" class="table-thumb" :src="imageSrc(row.image)" alt="" /></template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="salesCount" label="销量" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在售' : '已下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="说明" width="220" fixed="right">
        <template #default>
          <span class="muted-text">当前账号仅可查看，暂无发布/修改权限</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="loaded && !list.length" description="你还没有发布任何闲置物品" />
    <el-pagination
      v-if="total > pageSize"
      layout="prev, pager, next"
      :current-page="page"
      :total="total"
      :page-size="pageSize"
      @current-change="changePage"
    />

    <el-dialog v-model="visible" :title="editingId ? '编辑物品' : '发布新物品'" width="760px">
      <el-form :model="form" label-position="top">
        <el-form-item label="物品名称"><el-input v-model="form.name" placeholder="请输入物品名称" /></el-form-item>
        <el-form-item label="物品分类"><el-input v-model="form.category" placeholder="如：数码、书籍、生活用品" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位"><el-input v-model="form.unit" placeholder="件、台、套" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重量（克）"><el-input-number v-model="form.weightGrams" :min="0" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片">
          <ImageUpload v-model="form.image" :allow-url="false" />
          <span class="form-hint">请从本地上传图片，支持 jpg、png、webp、gif 格式，最大 5MB</span>
        </el-form-item>
        <el-form-item label="详情图片">
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
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="在售" inactive-text="已下架" /></el-form-item>
        <el-form-item label="物品简介"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要描述物品的成色、使用情况等" /></el-form-item>
        <el-form-item label="物品详情"><el-input v-model="form.detail" type="textarea" :rows="8" placeholder="支持 HTML 图文详情" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<!--
  文件说明：MyProducts.vue
  作用：学生发布和管理自己的闲置物品
  关键逻辑：
  - 查询、新增、编辑、删除学生自己发布的物品
  - 只能操作自己发布的物品，后端会校验权限
-->

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api/modules'
import ImageUpload from '../../components/ImageUpload.vue'

const list = ref([])
const total = ref(0)
const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const pageSize = 10
const editingId = ref(null)
const loaded = ref(false)
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
    const res = await userApi.myProducts({ page: page.value, size: pageSize })
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
    loaded.value = true
  } finally {
    loading.value = false
  }
}
const changePage = (value) => { page.value = value; load() }
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
    if (editingId.value) {
      await userApi.updateProduct(editingId.value, data)
    } else {
      await userApi.publishProduct(data)
    }
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该闲置物品？', '删除确认', { type: 'warning' })
  await userApi.deleteProduct(id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>