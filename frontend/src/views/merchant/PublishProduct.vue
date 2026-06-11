<template>
  <el-form :model="productForm" label-width="120px">
    <!-- 原有的 name, description, price 等字段保持不变 -->
    
    <!-- 在第X行后添加以下新字段 -->
    
    <!-- 新增：尺寸大小 -->
    <el-form-item label="尺寸大小">
      <el-input 
        v-model="productForm.size" 
        placeholder="例如：长20cm×宽15cm×高10cm" 
      />
    </el-form-item>
    
    <!-- 新增：新旧程度 -->
    <el-form-item label="新旧程度" required>
      <el-select v-model="productForm.conditionLevel" placeholder="请选择新旧程度">
        <el-option label="全新" value="BRAND_NEW" />
        <el-option label="九成新" value="LIKE_NEW" />
        <el-option label="八成新" value="GOOD" />
        <el-option label="七成新" value="FAIR" />
        <el-option label="六成新及以下" value="POOR" />
      </el-select>
    </el-form-item>
    
    <!-- 新增：是否允许议价 -->
    <el-form-item label="是否允许议价">
      <el-switch 
        v-model="productForm.allowBargain" 
        :active-value="1" 
        :inactive-value="0"
        active-text="允许"
        inactive-text="不允许"
      />
    </el-form-item>
    
    <!-- 新增：使用说明 -->
    <el-form-item label="使用说明">
      <el-input 
        v-model="productForm.usageInstructions" 
        type="textarea"
        :rows="4"
        placeholder="请输入商品的使用说明或注意事项"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'
import { merchantApi } from '../../api/modules'
import { ElMessage } from 'element-plus'

// 商品表单数据
const productForm = ref({
  name: '',
  description: '',
  image: '',
  category: '',
  tags: '',
  price: null,
  originalPrice: null,
  stock: 0,
  unit: '件',
  weightGrams: null,
  // 新增字段
  size: '',                    // 尺寸大小
  allowBargain: 0,             // 是否允许议价
  conditionLevel: '',          // 新旧程度
  usageInstructions: '',       // 使用说明
  detail: '',
  images: [],
  status: 1
})

// 提交商品发布
const handleSubmit = async () => {
  try {
    await merchantApi.publishProduct(productForm.value)
    ElMessage.success('商品发布成功')
    // 跳转到商品列表或其他页面
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  }
}
</script>