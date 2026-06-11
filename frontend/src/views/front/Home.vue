<template>
  <section class="hero" @mouseenter="pauseAuto" @mouseleave="resumeAuto">
    <div class="hero-container">
      <transition name="fade" mode="out-in">
        <div
          class="hero-slide"
          :key="activeBannerIndex"
          :style="slideStyle(activeBanner)"
        >
          <div class="hero-copy">
            <p>Campus Second-hand Market</p>
            <h1>{{ activeBanner?.title || company.companyName || '校园二手街' }}</h1>
            <span>{{ activeBanner?.subtitle || company.intro || '在校园里快速发布闲置物品、寻找学习用品、数码设备和宿舍好物，轻松完成交易与面交。' }}</span>
            <div class="hero-actions">
              <router-link to="/product">查看闲置物品</router-link>
              <router-link class="ghost-link" to="/user-login">学生登录</router-link>
            </div>
          </div>
        </div>
      </transition>

      <button v-if="banners.length > 1" class="hero-arrow left" @click="prevBanner" aria-label="上一张">‹</button>
      <button v-if="banners.length > 1" class="hero-arrow right" @click="nextBanner" aria-label="下一张">›</button>

      <div v-if="banners.length > 1" class="hero-dots" aria-label="Banner 切换">
        <button
          v-for="(item, index) in banners"
          :key="item.id"
          :class="{ active: index === activeBannerIndex }"
          type="button"
          @click="onDotClick(index)"
        />
      </div>
    </div>
  </section>

  <section class="section proof-row">
    <div><strong>{{ products.length || 0 }}</strong><span>闲置物品</span></div>
    <div><strong>{{ news.length || 0 }}</strong><span>校园贴士</span></div>
    <div><strong>面交</strong><span>线下取件</span></div>
    <div><strong>学生</strong><span>登录交易</span></div>
  </section>

  <section class="section two-col">
    <div>
      <p class="eyebrow">Campus Exchange</p>
      <h2>围绕学习、生活与宿舍场景提供校园闲置好物</h2>
    </div>
    <div class="value-list">
      <p>{{ company.culture || '校园二手交易平台聚焦实用、低价、便捷的闲置资源流转，支持图片展示、交易沟通、面交确认与评价反馈。' }}</p>
      <ul>
        <li>闲置物品支持封面、分类、标签、价格和状态展示</li>
        <li>学生登录后可浏览、联系并完成交易</li>
        <li>交易完成后可进行评价与协商，便于后续交流</li>
        <li>后台统一维护闲置物品、校园贴士、交易和咨询</li>
      </ul>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <div>
        <p class="eyebrow">Listings</p>
        <h2>精选闲置物品</h2>
      </div>
      <router-link to="/product">查看全部</router-link>
    </div>
    <div class="item-grid">
      <div v-for="item in products" :key="item.id" class="media-item">
        <router-link :to="`/product/${item.id}`">
          <img :src="imageSrc(item.image, productPlaceholder)" alt="" />
          <div class="product-meta"><span>{{ item.category || '闲置物品' }}</span><strong>￥{{ item.price || '0.00' }}</strong></div>
          <h3>{{ item.name }}</h3>
          <p>{{ item.description }}</p>
        </router-link>
        <div v-if="item.publisherType === 'merchant'" class="product-seller-info">
          <router-link :to="`/shop/${item.publisherId}`" class="seller-link">
            <el-tag size="small" type="success" effect="plain">店铺</el-tag>
            <span>{{ item.publisherName }}</span>
          </router-link>
        </div>
      </div>
    </div>
    <el-empty v-if="loaded && !products.length" description="暂无已发布闲置物品" />
  </section>

  <section class="section insight-band">
    <div>
      <p class="eyebrow">Workflow</p>
      <h2>从浏览闲置物品到完成交易，再到评价反馈，形成校园交易闭环</h2>
    </div>
    <div class="process">
      <span>物品发布</span>
      <span>学生浏览</span>
      <span>交易确认</span>
      <span>评价反馈</span>
    </div>
  </section>

  <section class="section">
    <div class="section-head">
      <div>
        <p class="eyebrow">Campus Notes</p>
        <h2>校园贴士</h2>
      </div>
      <router-link to="/news">进入校园贴士</router-link>
    </div>
    <div class="news-list">
      <router-link v-for="item in news" :key="item.id" :to="`/news/${item.id}`">
        <img v-if="item.coverImage" class="news-thumb" :src="imageSrc(item.coverImage, '')" alt="" />
        <time>{{ formatDate(item.createTime) }}</time>
        <h3>{{ item.title }}</h3>
        <p>{{ item.summary || brief(item.content) }}</p>
      </router-link>
    </div>
    <el-empty v-if="loaded && !news.length" description="暂无已发布贴士" />
  </section>
</template>

<!--
  文件说明：Home.vue
  作用：首页，聚合 banner、精选闲置物品、校园贴士和平台统计信息。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, ref, onUnmounted, watch } from 'vue'
import { publicApi } from '../../api/modules'

const company = ref({})
const banners = ref([])
const activeBannerIndex = ref(0)
const products = ref([])
const news = ref([])
const loaded = ref(false)
const productPlaceholder = 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=900&q=80'

const AUTO_PLAY_INTERVAL = Number(import.meta.env.VITE_BANNER_INTERVAL) || 5000
let timerId = null
let isPaused = false

const formatDate = (value) => value ? String(value).slice(0, 10) : ''
const brief = (value) => value ? String(value).replace(/\s+/g, ' ').slice(0, 90) : ''
const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}
const imageSrc = (value, fallback) => normalizeImageUrl(value) || fallback
const activeBanner = computed(() => banners.value[activeBannerIndex.value] || null)

const slideStyle = (banner) => {
  if (!banner || !banner.image) return {}
  return {
    backgroundImage: `linear-gradient(90deg, rgba(5,17,29,.92), rgba(5,17,29,.55), rgba(5,17,29,.12)), url("${imageSrc(banner.image, '')}")`
  }
}

const startAuto = () => {
  stopAuto()
  if (!banners.value || banners.value.length <= 1) return
  timerId = setInterval(() => {
    if (isPaused) return
    activeBannerIndex.value = (activeBannerIndex.value + 1) % banners.value.length
  }, AUTO_PLAY_INTERVAL)
}

const stopAuto = () => {
  if (timerId) {
    clearInterval(timerId)
    timerId = null
  }
}

const pauseAuto = () => { isPaused = true }
const resumeAuto = () => { isPaused = false }

const onDotClick = (index) => {
  activeBannerIndex.value = index
  // restart timer so user sees the selected banner for full interval
  startAuto()
}

const prevBanner = () => {
  if (!banners.value || banners.value.length <= 1) return
  activeBannerIndex.value = (activeBannerIndex.value - 1 + banners.value.length) % banners.value.length
  startAuto()
}

const nextBanner = () => {
  if (!banners.value || banners.value.length <= 1) return
  activeBannerIndex.value = (activeBannerIndex.value + 1) % banners.value.length
  startAuto()
}

watch(banners, (val) => {
  // start autoplay when banners loaded
  if (val && val.length > 1) startAuto()
  else stopAuto()
})

onMounted(async () => {
  const res = await publicApi.home()
  company.value = res.data?.company || {}
  banners.value = res.data?.banners || []
  products.value = res.data?.products || []
  news.value = res.data?.news || []
  loaded.value = true
})

onUnmounted(() => {
  stopAuto()
})
</script>

<style scoped>
/* make hero full-bleed horizontally while keeping content centered */
.hero { position: relative; overflow: hidden; min-height: 520px; width: 100vw; margin-left: calc(50% - 50vw); background: linear-gradient(90deg,#05111deb,#05111d8c,#05111d1f); }
.hero-container { position: relative; height: 100%; width: 100%; }
.hero-slide { position: relative; min-height: 520px; background-size: cover; background-position: center center; display: flex; align-items: center }
.hero-copy { padding: 48px; color: #fff; max-width: none; width: auto }
.hero-copy span { max-width: none }
.hero-actions { margin-top: 18px; display: flex; gap: 12px }
.hero-dots { position: absolute; left: 50%; transform: translateX(-50%); bottom: 18px; display: flex; gap: 8px }
.hero-dots button { width: 10px; height: 10px; border-radius: 50%; background: rgba(255,255,255,0.5); border: none }
.hero-dots button.active { background: #fff }
.hero-arrow { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.35); color: #fff; border: none; width: 40px; height: 56px; font-size: 28px; cursor: pointer }
.hero-arrow.left { left: 12px }
.hero-arrow.right { right: 12px }

/* fade transition */
.fade-enter-active, .fade-leave-active { transition: opacity 600ms ease }
.fade-enter-from, .fade-leave-to { opacity: 0 }

</style>

