<template>
  <section class="product-detail-hero">
    <div class="product-gallery">
      <img class="product-gallery-main" :src="imageSrc(activeImage?.url, placeholder)" :alt="activeImage?.caption || product.name || ''" />
      <div class="product-gallery-thumbs">
        <button
          v-for="(item, index) in galleryImages"
          :key="`${item.url}-${index}`"
          type="button"
          :class="{ active: index === activeImageIndex }"
          @click="activeImageIndex = index"
        >
          <img :src="imageSrc(item.url, placeholder)" :alt="item.caption || product.name || ''" />
        </button>
      </div>
    </div>

    <div class="product-detail-info">
      <p>Campus Trade</p>
      <h1>{{ product.name || '闲置物品详情' }}</h1>
      <span>{{ product.description }}</span>
      <strong class="detail-price">¥{{ product.price || '0.00' }}<em v-if="product.unit">/{{ product.unit }}</em></strong>
      <del v-if="product.originalPrice && product.originalPrice > product.price" class="detail-original">¥{{ product.originalPrice }}</del>
      <div class="detail-metrics compact">
        <div><strong>{{ product.stock ?? 0 }}</strong><span>可交易数</span></div>
        <div><strong>{{ product.salesCount || 0 }}</strong><span>成交次数</span></div>
        <div><strong>{{ product.category || '闲置物品' }}</strong><span>物品分类</span></div>
      </div>
      <!-- 商品额外信息 -->
      <div class="product-extra-info" style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap; align-items: center;">
        <el-tag v-if="product.conditionLevel" type="warning" size="large">
          {{ getConditionText(product.conditionLevel) }}
        </el-tag>
        <span v-if="product.size" style="color: #606266; font-size: 14px;">
          📐 尺寸：{{ product.size }}
        </span>
        <el-tag v-if="product.allowBargain === 1" type="success">允许议价</el-tag>
      </div>
      <div class="tag-row">
        <span v-for="tag in tags(product.tags)" :key="tag">{{ tag }}</span>
      </div>
      <!-- 发布者/店铺信息 -->
      <div v-if="product.publisherName" class="product-seller-info">
        <template v-if="product.publisherType === 'merchant'">
          <router-link :to="`/shop/${product.publisherId}`" class="seller-link">
            <el-avatar v-if="product.publisherAvatar" :size="28" :src="imageSrc(product.publisherAvatar, '')" />
            <el-avatar v-else :size="28" style="background:#409EFF;vertical-align:middle;">
              {{ product.publisherName.charAt(0) }}
            </el-avatar>
            <span class="seller-name">{{ product.publisherName }}</span>
            <el-tag size="small" type="success" effect="plain">店铺</el-tag>
          </router-link>
        </template>
        <div v-else class="seller-customer">
          <el-avatar :size="28" style="background:#67C23A;">{{ product.publisherName?.charAt(0) }}</el-avatar>
          <span class="seller-name">{{ product.publisherName }}</span>
          <el-tag size="small" type="info" effect="plain">学生发布</el-tag>
        </div>
      </div>
      <div class="product-actions">
        <button class="buy-button secondary" type="button" @click="addCart">加入交易清单</button>
        <button v-if="product.allowBargain === 1" class="buy-button bargain" type="button" @click="openBargainDialog">议价</button>
        <button class="buy-button" type="button" @click="openBuyDialog">立即购买</button>
      </div>
    </div>
  </section>

  <section class="section product-tabs-section">
    <el-tabs v-model="activeTab" class="product-tabs">
      <el-tab-pane label="物品详情" name="detail">
        <article class="article-body product-detail-body" v-html="safeDetail"></article>
        <el-empty v-if="!safeDetail" description="暂无物品详情" />
      </el-tab-pane>
      <!-- 使用说明标签页 -->
      <el-tab-pane v-if="product.usageInstructions" label="使用说明" name="usage">
        <article class="article-body product-detail-body" v-html="safeUsageInstructions"></article>
      </el-tab-pane>
      <el-tab-pane :label="`交易评价（${reviews.length}）`" name="reviews">
        <div v-if="reviews.length" class="product-review-list">
          <article v-for="review in reviews" :key="review.id" class="product-review-card">
            <div class="product-review-head">
              <div>
                <strong>{{ review.customerName || '匿名用户' }}</strong>
                <time>{{ formatDate(review.createTime) }}</time>
              </div>
              <el-tag size="small" :type="review.reviewType === 'ADDITIONAL' ? 'warning' : 'success'">
                {{ review.reviewType === 'ADDITIONAL' ? '追评' : '主评' }}
              </el-tag>
            </div>
            <div class="product-review-rates">
              <span>质量 <el-rate :model-value="review.qualityRating" disabled /></span>
              <span>服务 <el-rate :model-value="review.serviceRating" disabled /></span>
              <span>物流 <el-rate :model-value="review.logisticsRating" disabled /></span>
              <span v-if="review.serviceAttitudeRating">服务态度 <el-rate :model-value="review.serviceAttitudeRating" disabled /></span>
            </div>
            <p>{{ review.content || '用户没有填写文字评价。' }}</p>
            <div v-if="reviewMedia(review).length" class="product-review-media">
              <template v-for="item in reviewMedia(review)" :key="item.url">
                <img v-if="item.type === 'image'" :src="imageSrc(item.url, '')" alt="" />
                <video v-else :src="imageSrc(item.url, '')" controls />
              </template>
            </div>
          </article>
        </div>
        <el-empty v-else description="暂无评论" />
      </el-tab-pane>
      <el-tab-pane label="交易说明" name="service">
        <div class="service-notes">
          <div><strong>交易协商</strong><span>登录后可在我的交易中发起换货、退货、维修或面交协商。</span></div>
          <div><strong>库存与取件</strong><span>提交交易时会校验可交易数量，平台支持面交与自取流程。</span></div>
          <div><strong>交易评价</strong><span>确认完成后可提交交易评价，评价会显示在当前物品详情页。</span></div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </section>

  <el-dialog v-model="visible" title="确认交易" width="520px">
    <div v-if="acceptedBargainPrice" style="background: #fdf6ec; padding: 12px; border-radius: 6px; margin-bottom: 16px; color: #e6a23c;">
      ✅ 议价已通过！单价将使用议价价格：<strong>¥{{ acceptedBargainPrice }}</strong>（原价 ¥{{ product.price }}）
    </div>
    <el-form label-position="top">
      <el-form-item label="购买数量"><el-input-number v-model="order.quantity" :min="1" /></el-form-item>
      <el-form-item label="收货地址">
        <el-select v-model="order.addressId" placeholder="请选择收货地址" style="width: 100%">
          <el-option v-for="item in addresses" :key="item.id" :label="addressLabel(item)" :value="item.id" />
        </el-select>
      </el-form-item>
      <span class="form-hint">可在顶部账号菜单中维护地址</span>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="buy">提交交易</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="bargainVisible" title="议价出价" width="420px">
    <p style="margin-bottom: 12px; color: #606266;">
      商品原价：<strong style="color: #f56c6c;">¥{{ product.price }}</strong>
      ，请输入您的期望价格：
    </p>
    <el-form>
      <el-form-item label="我的出价">
        <el-input-number v-model="bargainPrice" :min="0.01" :precision="2" :max="product.price ? product.price - 0.01 : undefined" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="bargainVisible = false">取消</el-button>
      <el-button type="primary" :loading="bargainSaving" @click="submitBargain">提交议价</el-button>
    </template>
  </el-dialog>
</template>
<!--
  文件说明：ProductDetail.vue
  作用：闲置物品详情页，展示图片、详情、评价和购买操作。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DOMPurify from 'dompurify'
import { ElMessage } from 'element-plus'
import { publicApi, userApi } from '../../api/modules'

const route = useRoute() // 路由参数用于获取当前商品 ID
const router = useRouter() // 路由实例用于跳转登录页、订单页等
const product = ref({}) // 当前商品详情数据
const reviews = ref([]) // 当前商品对应的用户评价列表
const visible = ref(false) // 购买确认弹窗显示状态
const saving = ref(false) // 提交交易时的按钮加载状态
const activeTab = ref('detail') // 详情页当前激活的标签页
const activeImageIndex = ref(0) // 当前主图索引，决定放大展示哪张图片
const order = reactive({ quantity: 1, addressId: null }) // 订单提交时的购买数量和收货地址选择
const addresses = ref([]) // 当前用户地址列表
const bargainOfferId = ref(route.query.bargainOfferId ? Number(route.query.bargainOfferId) : null) // 议价出价ID（从议价记录跳过来时使用）
const acceptedBargainPrice = ref(route.query.bargainPrice ? Number(route.query.bargainPrice) : null) // 已接受的议价价格
const bargainVisible = ref(false) // 议价弹窗显示状态
const bargainPrice = ref(0) // 买家议价出价
const bargainSaving = ref(false) // 议价提交按钮加载状态
const placeholder = 'https://images.unsplash.com/photo-1515955656352-a1fa3ffcd111?auto=format&fit=crop&w=1200&q=80' // 商品图片缺失时使用的占位图

const tags = (value) => value ? String(value).split(',').map(item => item.trim()).filter(Boolean) : [] // 将标签字符串拆分成数组，方便页面渲染标签

const galleryImages = computed(() => {
  // 先把封面图放进图片列表，确保主图和缩略图都有展示内容
  const items = []
  if (product.value.image) {
    items.push({ url: product.value.image, caption: '封面图' })
  }
  for (const item of product.value.images || []) {
    if (item.imageUrl && !items.some(existing => existing.url === item.imageUrl)) {
      items.push({ url: item.imageUrl, caption: item.caption || '' })
    }
  }
  return items.length ? items : [{ url: placeholder, caption: '闲置物品图片' }]
})
const activeImage = computed(() => galleryImages.value[activeImageIndex.value] || galleryImages.value[0]) // 当前选中的图片用于主图展示
const safeDetail = computed(() => DOMPurify.sanitize(product.value.detail || product.value.description || '', {
  ADD_TAGS: ['figure', 'figcaption'],
  ADD_ATTR: ['target', 'rel']
})) // 过滤富文本中的危险标签，防止详情内容注入问题
// 使用说明的安全过滤
const safeUsageInstructions = computed(() => DOMPurify.sanitize(product.value.usageInstructions || '', {
  ADD_TAGS: ['figure', 'figcaption'],
  ADD_ATTR: ['target', 'rel']
}))
const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}
const imageSrc = (value, fallback) => normalizeImageUrl(value) || fallback // 将相对路径转换为可访问的图片地址，找不到时回退到占位图

const addressLabel = (address) => `${address.receiverName} ${address.phone} ${[address.province, address.city, address.district, address.detailAddress].filter(Boolean).join('')}` // 统一拼接收货地址文本用于下拉列表展示

const formatDate = (value) => {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const reviewMedia = (review) => {
  if (!review.mediaUrls) return [] // 评价中可能没有上传图片或视频
  try {
    const parsed = JSON.parse(review.mediaUrls)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return [] // 解析失败时回退为空数组，避免页面报错
  }
}

const loadAddresses = async () => {
  // 加载用户地址列表
  addresses.value = (await userApi.addresses()).data || []
  // 查找默认地址，不存在则取第一个
  const defaultAddress = addresses.value.find(item => item.isDefault === 1) || addresses.value[0]
  // 设置订单地址ID
  order.addressId = defaultAddress?.id || null
}

// 打开购买对话框
const openBuyDialog = async () => {
  // 未登录用户不能发起交易，需要跳转到登录页
  if (!localStorage.getItem('user_token')) {
    ElMessage.warning('请先登录会员账号')
    router.push('/user-login')
    return
  }
  // 打开购买弹窗前先拉取地址信息，便于用户选择收货地点
  await loadAddresses()
  // 没有地址时提示先维护地址
  if (!addresses.value.length) {
    ElMessage.warning('请先在账号菜单中添加收货地址')
    return
  }
  // 展示确认交易弹窗
  visible.value = true
}

const buy = async () => {
  if (!localStorage.getItem('user_token')) {
    ElMessage.warning('请先登录会员账号')
    router.push('/user-login')
    return
  }
  if (!order.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  saving.value = true // 锁定按钮，避免重复提交订单
  try {
    const orderData = { ...order, productId: product.value.id }
    // 若从议价记录跳来，传入议价ID以便后端使用议价价格
    if (bargainOfferId.value) {
      orderData.bargainOfferId = bargainOfferId.value
    }
    await userApi.createOrder(orderData)
    ElMessage.success(acceptedBargainPrice.value ? '已按议价价格下单，可在我的交易中查看' : '交易成功，可在我的交易中查看')
    visible.value = false
    router.push('/orders')
  } finally {
    saving.value = false
  }
}

const addCart = async () => {
  if (!localStorage.getItem('user_token')) {
    ElMessage.warning('请先登录会员账号')
    router.push('/user-login')
    return
  }
  await userApi.addCartItem({ productId: product.value.id, quantity: 1 })
  window.dispatchEvent(new Event('cart-changed')) // 通知其他页面更新购物车角标
  ElMessage.success('已加入交易清单')
}

const openBargainDialog = () => {
  if (!localStorage.getItem('user_token')) {
    ElMessage.warning('请先登录会员账号')
    router.push('/user-login')
    return
  }
  bargainVisible.value = true
}

const submitBargain = async () => {
  if (!bargainPrice.value || bargainPrice.value <= 0) {
    ElMessage.warning('请输入有效的出价')
    return
  }
  bargainSaving.value = true
  try {
    await userApi.createBargainOffer(product.value.id, bargainPrice.value)
    ElMessage.success('议价已提交，请等待商家回复')
    bargainVisible.value = false
  } catch (e) {
    // 后端会返回错误信息
  } finally {
    bargainSaving.value = false
  }
}

onMounted(async () => {
  const productId = route.params.id // 当前详情页路由中的商品 ID
  const [productRes, reviewRes] = await Promise.all([
    publicApi.productDetail(productId), // 加载商品详情数据
    publicApi.productReviews(productId) // 同时加载商品评价
  ])
  product.value = productRes.data || {}
  reviews.value = reviewRes.data || []
  activeImageIndex.value = 0 // 重置主图为第一张
  // 初始化议价默认值（原价的一半）
  bargainPrice.value = product.value.price ? Math.round(product.value.price * 50) / 100 : 0
})
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
</script>

<style scoped>
.product-seller-info {
  margin: 12px 0;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.seller-link,
.seller-customer {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: inherit;
}
.seller-link:hover .seller-name {
  color: #409EFF;
}
.seller-name {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
  transition: color 0.2s;
}
.seller-link:hover {
  cursor: pointer;
}
</style>
