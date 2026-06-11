<template>
  <div class="shop-detail">
    <el-card v-if="loading" class="box-card">加载中...</el-card>
    <el-card v-else class="box-card">
      <!-- 店铺基本信息 -->
      <div style="display:flex;align-items:center;gap:20px;margin-bottom:20px;">
        <el-avatar v-if="shop.shopLogo" :size="100" :src="normalizeImageUrl(shop.shopLogo)" shape="square" />
        <div v-else class="text-avatar" :style="{ background: avatarColor }">{{ avatarText }}</div>
        <div style="flex:1;">
          <h2>{{ shop.shopName || '店铺' }}</h2>
          <div style="color:var(--el-text-color-secondary);margin:8px 0;">
            {{ shop.shopDescription || '暂无描述' }}
          </div>
          <div style="display:flex;gap:12px;flex-wrap:wrap;">
            <el-tag :type="getMerchantLevelType(shop.merchantLevel)" size="large">
              {{ getMerchantLevelText(shop.merchantLevel) }}
            </el-tag>
            <el-tag type="info" size="large">好评率 {{ shop.positiveRate || 0 }}%</el-tag>
          </div>
        </div>
      </div>

      <!-- 店铺统计信息 -->
      <el-row :gutter="16" style="margin-bottom:20px;">
        <el-col :span="6">
          <el-statistic title="店铺评分" :value="shop.shopRating || 0">
            <template #suffix>分</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="服务态度" :value="shop.serviceAttitudeRating || 0">
            <template #suffix>分</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="总销量" :value="shop.totalSales || 0">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="评价总数" :value="shop.reviewCount || 0">
            <template #suffix>条</template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-divider />

      <!-- 商品列表 -->
      <div>
        <h3>在售商品 ({{ shop.onSaleProductCount || 0 }}件)</h3>
        <el-row :gutter="16">
          <el-col :span="6" v-for="item in shop.products?.content || []" :key="item.id">
            <el-card class="product-card" shadow="hover">
              <router-link :to="`/product/${item.id}`" style="text-decoration:none;color:inherit;">
                <img :src="normalizeImageUrl(item.image)" alt="" style="width:100%;height:140px;object-fit:cover;border-radius:4px;" />
                <div style="margin-top:8px;font-weight:500;">{{ item.name }}</div>
                <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px;">
                  <span style="color:#F56C6C;font-weight:bold;">¥{{ item.price || 0 }}</span>
                  <span style="color:var(--el-text-color-secondary);font-size:12px;">已售 {{ item.salesCount || 0 }}</span>
                </div>
                <div style="margin-top:4px;">
                  <el-tag v-if="item.conditionLevel" size="small" type="warning">
                    {{ getConditionText(item.conditionLevel) }}
                  </el-tag>
                  <el-tag v-if="item.allowBargain === 1" size="small" type="success" style="margin-left:4px;">
                    可议价
                  </el-tag>
                </div>
              </router-link>
            </el-card>
          </el-col>
        </el-row>
        <div v-if="!shop.products?.content?.length" style="color:var(--el-text-color-secondary);margin-top:12px;text-align:center;">
          暂无商品
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="(shop.products?.totalElements || 0) > 12"
        layout="prev, pager, next"
        :current-page="currentPage"
        :total="shop.products?.totalElements || 0"
        :page-size="12"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 店铺评价 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <h3>店铺评价 ({{ reviewTotal }}条)</h3>
      <el-divider />
      <div v-if="reviewLoading">加载中...</div>
      <div v-else-if="reviews.length">
        <div v-for="review in reviews" :key="review.id" class="shop-review-card">
          <div class="shop-review-head">
            <strong>{{ review.customerName || '匿名用户' }}</strong>
            <span style="color:var(--el-text-color-secondary);font-size:13px;">{{ formatDate(review.createTime) }}</span>
            <el-tag v-if="review.reviewType === 'ADDITIONAL'" size="small" type="warning" style="margin-left:8px;">追评</el-tag>
            <router-link :to="`/product/${review.productId}`" style="margin-left:auto;font-size:13px;color:#409EFF;text-decoration:none;">
              查看商品
            </router-link>
          </div>
          <div class="shop-review-rates">
            <span>质量 <el-rate :model-value="review.qualityRating" disabled /></span>
            <span>服务 <el-rate :model-value="review.serviceRating" disabled /></span>
            <span>物流 <el-rate :model-value="review.logisticsRating" disabled /></span>
            <span v-if="review.serviceAttitudeRating">态度 <el-rate :model-value="review.serviceAttitudeRating" disabled /></span>
          </div>
          <p>{{ review.content || '用户没有填写文字评价。' }}</p>
        </div>
        <el-pagination
          v-if="reviewTotal > 5"
          layout="prev, pager, next"
          :current-page="reviewPage"
          :total="reviewTotal"
          :page-size="5"
          @current-change="(page) => { reviewPage = page; loadReviews() }"
          style="margin-top: 16px; justify-content: center;"
        />
      </div>
      <el-empty v-else description="暂无评价" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { publicApi } from '../../api/modules'

const route = useRoute()
const loading = ref(true)
const shop = ref({})
const currentPage = ref(1)

// 评价相关
const reviews = ref([])
const reviewPage = ref(1)
const reviewTotal = ref(0)
const reviewLoading = ref(false)

// 根据店铺名称生成文字头像
const avatarText = computed(() => {
  const name = shop.value.shopName || shop.value.shopDescription || '店铺'
  return name.charAt(0).toUpperCase()
})
const avatarColor = computed(() => {
  const seed = shop.value.shopName || 'shop'
  const colors = [
    '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
    '#00BCD4', '#9C27B0', '#FF5722', '#3F51B5', '#009688',
    '#795548', '#607D8B', '#E91E63', '#2196F3', '#4CAF50'
  ]
  let hash = 0
  for (let i = 0; i < seed.length; i++) {
    hash = seed.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
})

const load = async () => {
  loading.value = true
  try {
    const merchantId = route.params.merchantId
    const res = await publicApi.shopDetail(merchantId, { 
      page: currentPage.value, 
      size: 12 
    })
    shop.value = res.data || {}
  } catch (e) {
    shop.value = {}
  } finally {
    loading.value = false
  }
}

// 加载店铺评价
const loadReviews = async () => {
  const merchantId = route.params.merchantId
  reviewLoading.value = true
  try {
    const res = await publicApi.shopReviews(merchantId, { page: reviewPage.value, size: 5 })
    reviews.value = res.data?.content || []
    reviewTotal.value = res.data?.totalElements || 0
  } catch {
    reviews.value = []
  } finally {
    reviewLoading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  load()
}

const getMerchantLevelText = (level) => {
  const map = {
    'BRONZE': '铜牌商家',
    'SILVER': '银牌商家',
    'GOLD': '金牌商家',
    'DIAMOND': '钻石商家'
  }
  return map[level] || '未评级'
}

const getMerchantLevelType = (level) => {
  const map = {
    'BRONZE': '',
    'SILVER': 'info',
    'GOLD': 'warning',
    'DIAMOND': 'success'
  }
  return map[level] || ''
}

const formatDate = (value) => {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}

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

onMounted(() => {
  load()
  loadReviews()
})
watch(() => route.params.merchantId, () => {
  currentPage.value = 1
  reviewPage.value = 1
  load()
  loadReviews()
})
</script>

<style scoped>
.product-card { 
  cursor: pointer;
  transition: transform 0.2s;
}
.product-card:hover {
  transform: translateY(-4px);
}
.text-avatar {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 42px;
  font-weight: 700;
  flex-shrink: 0;
  letter-spacing: 2px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
.shop-review-card {
  padding: 14px 0;
  border-bottom: 1px solid #ebeef5;
}
.shop-review-card:last-child {
  border-bottom: none;
}
.shop-review-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.shop-review-rates {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 6px;
}
.shop-review-rates span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}
.shop-review-card p {
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  margin: 6px 0 0;
}
</style>