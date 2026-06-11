<template>
  <section class="page-title">
    <p>Second-hand Listings</p>
    <h1>闲置物品</h1>
    <span>校园二手平台展示课程资料、数码设备、生活用品和宿舍好物，登录后即可查看并完成交易。</span>
  </section>
  <section class="section">
    <div class="search-bar product-filter">
      <el-input v-model="keyword" placeholder="搜索闲置物品名称" clearable @clear="search" @keyup.enter="search" />
      <el-select v-model="category" placeholder="全部种类" clearable @change="search" @clear="search">
        <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="sort" placeholder="排序方式" @change="search">
        <el-option label="最新上架" value="newest" />
        <el-option label="价格从低到高" value="priceAsc" />
        <el-option label="价格从高到低" value="priceDesc" />
        <el-option label="销量优先" value="salesDesc" />
        <el-option label="好评率优先" value="positiveRateDesc" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>
    <div class="item-grid list-page">
      <div v-for="item in list" :key="item.id" class="media-item">
        <router-link :to="`/product/${item.id}`">
          <img :src="imageSrc(item.image, placeholder)" alt="" />
          <div class="product-meta">
            <span>{{ item.category || '闲置物品' }}</span>
            <strong>￥{{ item.price || '0.00' }}<em v-if="item.unit">/{{ item.unit }}</em></strong>
          </div>
          <h3>{{ item.name }}</h3>
          <p>{{ item.description }}</p>
          <div class="product-stat">
            <span>库存 {{ item.stock ?? 0 }}</span>
            <span>销量 {{ item.salesCount || 0 }}</span>
            <span v-if="item.positiveRate !== undefined">好评率 {{ item.positiveRate }}%</span>
            <del v-if="item.originalPrice && item.originalPrice > item.price">￥{{ item.originalPrice }}</del>
          </div>
          <div class="tag-row compact"><span v-for="tag in tags(item.tags)" :key="tag">{{ tag }}</span></div>
        </router-link>
        <div v-if="item.publisherType === 'merchant'" class="product-seller-info">
          <router-link :to="`/shop/${item.publisherId}`" class="seller-link">
            <el-tag size="small" type="success" effect="plain">店铺</el-tag>
            <span>{{ item.publisherName }}</span>
          </router-link>
        </div>
      </div>
    </div>
    <el-empty v-if="loaded && !list.length" description="暂无闲置物品" />
    <el-pagination
      v-if="total > pageSize"
      layout="prev, pager, next"
      :current-page="page"
      :total="total"
      :page-size="pageSize"
      @current-change="changePage"
    />
  </section>
</template>

<!--
  文件说明：Products.vue
  作用：闲置物品列表页，支持搜索、分类筛选、排序和分页。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, ref } from 'vue'
import { publicApi } from '../../api/modules'

const keyword = ref('')
const category = ref('')
const sort = ref('newest')
const categories = ref([])
const page = ref(1)
const pageSize = 9
const total = ref(0)
const list = ref([])
const loaded = ref(false)
const placeholder = 'https://images.unsplash.com/photo-1515955656352-a1fa3ffcd111?auto=format&fit=crop&w=900&q=80'
const tags = (value) => value ? String(value).split(',').map(item => item.trim()).filter(Boolean) : []

const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}
const imageSrc = (value, fallback) => normalizeImageUrl(value) || fallback
const load = async () => {
  const res = await publicApi.products({
    keyword: keyword.value,
    category: category.value,
    sort: sort.value,
    page: page.value,
    size: pageSize
  })
  list.value = res.data?.content || []
  total.value = res.data?.totalElements || 0
  loaded.value = true
}
const loadCategories = async () => {
  categories.value = (await publicApi.productCategories()).data || []
}
const search = () => { page.value = 1; load() }
const changePage = (value) => { page.value = value; load() }
onMounted(async () => {
  await loadCategories()
  await load()
})
</script>
