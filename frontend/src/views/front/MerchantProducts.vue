<template>
  <section class="page-title">
    <p>Merchant Center</p>
    <h1>我的店铺商品管理</h1>
    <span>管理您店铺中的商品，可以发布新商品、编辑现有商品或上下架商品。</span>
  </section>
  
  <section class="section">
    <div class="toolbar">
      <div>
        <h2>商品列表</h2>
        <p class="subtle-text">当前登录：{{ merchantName }}</p>
      </div>
      <div class="toolbar-actions">
        <el-button type="success" @click="goToShop">查看店铺</el-button>
        <el-button type="primary" @click="openProduct()">发布新商品</el-button>
        <el-button @click="() => router.push('/merchant-orders')">订单管理</el-button>
        <el-button @click="goToSettings">商家信息</el-button>
        <el-button type="danger" plain @click="logout">退出登录</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="productList" style="width: 100%">
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <img v-if="row.image" class="table-thumb" :src="imageSrc(row.image)" alt="" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column label="新旧程度" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.conditionLevel" size="small" type="warning">
            {{ getConditionText(row.conditionLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.auditStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '在售' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openProduct(row)">编辑</el-button>
          <el-button 
            size="small" 
            :type="row.status === 1 ? 'warning' : 'success'"
            :disabled="row.status !== 1 && row.auditStatus !== 1"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <span v-if="row.status !== 1 && row.auditStatus !== 1" style="display:block;color:#909399;font-size:11px;margin-top:2px;">
            {{ row.auditStatus === 2 ? '审核被拒绝，无法上架' : '等待管理员审核后上架' }}
          </span>
          <el-button size="small" type="danger" @click="removeProduct(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="loaded && !productList.length" description="您还没有发布任何商品" />
    
    <el-pagination
      v-if="total > pageSize"
      layout="prev, pager, next"
      :current-page="page"
      :total="total"
      :page-size="pageSize"
      @current-change="changePage"
      style="margin-top: 20px; justify-content: center;"
    />

    <!-- 商品编辑对话框 -->
    <el-dialog 
      v-model="productDialogVisible" 
      :title="editingProductId ? '编辑商品' : '发布新商品'" 
      width="800px"
    >
      <el-form :model="productForm" label-position="top">
        <el-form-item label="商品名称">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品分类">
              <el-input v-model="productForm.category" placeholder="如：数码、书籍、生活用品" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="productForm.tags" placeholder="多个标签用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="价格">
              <el-input-number v-model="productForm.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number v-model="productForm.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="productForm.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="尺寸大小">
              <el-input v-model="productForm.size" placeholder="如：20×15×10cm" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="新旧程度">
              <el-select v-model="productForm.conditionLevel" placeholder="请选择" style="width: 100%">
                <el-option label="全新" value="BRAND_NEW" />
                <el-option label="九成新" value="LIKE_NEW" />
                <el-option label="八成新" value="GOOD" />
                <el-option label="七成新" value="FAIR" />
                <el-option label="六成新及以下" value="POOR" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="允许议价">
              <el-switch v-model="productForm.allowBargain" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="productForm.unit" placeholder="件、台、套" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重量（克）">
              <el-input-number v-model="productForm.weightGrams" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面图片">
          <ImageUpload v-model="productForm.image" :allow-url="false" />
          <span class="form-hint">请从本地上传图片，支持 jpg、png、webp、gif 格式，最大 5MB</span>
        </el-form-item>

        <el-form-item label="详情图片">
          <div class="gallery-editor">
            <div v-for="(item, index) in productForm.images" :key="index" class="gallery-editor-item">
              <ImageUpload v-model="item.imageUrl" :allow-url="false" />
              <el-input v-model="item.caption" placeholder="图片说明，可选" style="margin: 10px 0;" />
              <el-input-number v-model="item.sort" :min="0" style="margin-right: 10px;" />
              <el-button type="danger" @click="removeGalleryImage(index)">删除</el-button>
            </div>
            <el-button @click="addGalleryImage">添加详情图片</el-button>
          </div>
        </el-form-item>

        <el-form-item label="商品简介">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="简要描述商品的成色、使用情况等" />
        </el-form-item>

        <el-form-item label="使用说明">
          <el-input v-model="productForm.usageInstructions" type="textarea" :rows="4" placeholder="请输入商品的使用说明或注意事项" />
        </el-form-item>

        <el-form-item label="商品详情">
          <el-input v-model="productForm.detail" type="textarea" :rows="8" placeholder="支持 HTML 图文详情" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { merchantApi } from '../../api/modules'
import ImageUpload from '../../components/ImageUpload.vue'

const router = useRouter()
const merchantName = computed(() => localStorage.getItem('merchant_name') || '商家')
const productList = ref([])
const total = ref(0)
const productDialogVisible = ref(false)
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const pageSize = 10
const editingProductId = ref(null)
const loaded = ref(false)

const emptyProductForm = {
  sku: '',
  name: '',
  category: '',
  tags: '',
  image: '',
  images: [],
  price: 0,
  originalPrice: 0,
  stock: 0,
  unit: '件',
  salesCount: 0,
  weightGrams: 0,
  size: '',                    
  conditionLevel: '',          
  allowBargain: 0,             
  usageInstructions: '',       
  description: '',
  detail: '',
  status: 1
}

const productForm = reactive({ ...emptyProductForm })

const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}

const imageSrc = (value) => normalizeImageUrl(value)

const loadProducts = async () => {
  loading.value = true
  try {
    const res = await merchantApi.myProducts({ page: page.value, size: pageSize })
    productList.value = res.data.content || []
    total.value = res.data.totalElements || 0
    loaded.value = true
  } finally {
    loading.value = false
  }
}

const changePage = (value) => {
  page.value = value
  loadProducts()
}

const goToShop = () => {
  merchantApi.getShopInfo().then(res => {
    router.push({ name: 'ShopDetail', params: { merchantId: res.data.id } })
  })
}

const goToSettings = () => {
  router.push('/merchant-shop-settings')
}

const logout = async () => {
  try {
    await merchantApi.logout().catch(() => {})
  } finally {
    localStorage.removeItem('merchant_token')
    localStorage.removeItem('merchant_name')
    localStorage.removeItem('merchant_info')
    ElMessage.success('已退出登录')
    router.push('/merchant-login')
  }
}

const openProduct = (row) => {
  editingProductId.value = row?.id || null
  Object.assign(productForm, emptyProductForm, row || {}, {
    images: normalizeImages(row?.images)
  })
  productDialogVisible.value = true
}

const normalizeImages = (images) => 
  Array.isArray(images)
    ? images.map((item, index) => ({
        imageUrl: item.imageUrl || '',
        caption: item.caption || '',
        sort: item.sort ?? index
      }))
    : []

const addGalleryImage = () => {
  productForm.images.push({ imageUrl: '', caption: '', sort: productForm.images.length })
}

const removeGalleryImage = (index) => {
  productForm.images.splice(index, 1)
}

const productPayload = () => ({
  ...productForm,
  images: productForm.images
    .filter(item => item.imageUrl)
    .map((item, index) => ({ ...item, sort: item.sort ?? index }))
})

const saveProduct = async () => {
  if (!productForm.name || !productForm.description) {
    ElMessage.warning('请填写商品名称和简介')
    return
  }
  
  console.log('发布商品数据:', productPayload()) // 添加这行
  console.log('商家Token:', localStorage.getItem('merchant_token')) // 添加这行
  
  saving.value = true
  try {
    const data = productPayload()
    if (editingProductId.value) {
      await merchantApi.updateProduct(editingProductId.value, data)
      ElMessage.success('商品更新成功')
    } else {
      console.log('调用发布API:', data) // 添加这行
      await merchantApi.publishProduct(data)
      ElMessage.success('商品发布成功')
    }
    productDialogVisible.value = false
    loadProducts()
  } catch (error) {
    console.error('发布失败:', error) // 添加这行
    ElMessage.error('发布失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'

  // 上架前检查审核状态
  if (newStatus === 1 && row.auditStatus !== 1) {
    ElMessage.warning(row.auditStatus === 2
      ? '该商品审核被拒绝，无法上架'
      : '该商品尚未通过管理员审核，无法上架')
    return
  }

  try {
    await ElMessageBox.confirm(`确认${action}该商品吗？`, `${action}确认`, { type: 'warning' })
    await merchantApi.updateProductStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadProducts()
  } catch {
    // 用户取消操作
  }
}

const removeProduct = async (id) => {
  await ElMessageBox.confirm('确认删除该商品吗？此操作不可恢复。', '删除确认', { type: 'warning' })
  await merchantApi.deleteProduct(id)
  ElMessage.success('商品已删除')
  loadProducts()
}
// 新旧程度文本转换
const getConditionText = (level) => {
  const map = {
    'BRAND_NEW': '全新',
    'LIKE_NEW': '九成新',
    'GOOD': '八成新',
    'FAIR': '七成新',
    'POOR': '六成新及以下'
  }
  return map[level] || level
}
onMounted(loadProducts)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.subtle-text {
  margin: 4px 0 0;
  color: #909399;
  font-size: 12px;
}

.gallery-editor {
  width: 100%;
}

.gallery-editor-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
}

.table-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.form-hint {
  display: block;
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>