<template>
  <div class="shop-detail">
    <el-card v-if="loading" class="box-card">加载中...</el-card>
    <el-card v-else class="box-card">
      <!-- 店铺基本信息 -->
      <div style="display:flex;align-items:center;gap:20px;margin-bottom:20px;">
        <el-avatar :size="100" :src="shop.shopLogo" shape="square" />
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
                <img :src="item.image || ''" alt="" style="width:100%;height:140px;object-fit:cover;border-radius:4px;" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { publicApi } from '../../api/modules'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const shop = ref({})
const currentPage = ref(1)

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

onMounted(load)
watch(() => route.params.merchantId, load)
</script>

<style scoped>
.product-card { 
  cursor: pointer;
  transition: transform 0.2s;
}
.product-card:hover {
  transform: translateY(-4px);
}
</style>