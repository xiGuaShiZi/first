<template>
  <section class="page-title">
    <p>Review</p>
    <h1>交易评价</h1>
    <span>请诚实、客观地分享交易体验。匿名评价会隐藏用户ID，但评价内容仍会展示。</span>
  </section>

  <section class="section review-page">
    <el-alert
      v-if="submitted"
      title="该交易已评价，提交后的评价不可修改或删除。"
      type="success"
      show-icon
      :closable="false"
    />

    <el-form v-else class="review-form" label-position="top">
      <div class="review-product">
        <div>
          <p class="eyebrow">Order</p>
          <h2>{{ order?.productName || '交易物品' }}</h2>
          <span>{{ order?.orderNo }}</span>
        </div>
        <strong v-if="order">¥{{ order.totalAmount }}</strong>
      </div>

      <el-form-item label="物品质量">
        <el-rate v-model="form.qualityRating" />
      </el-form-item>
      <el-form-item label="卖家服务">
        <el-rate v-model="form.serviceRating" />
      </el-form-item>
      <el-form-item label="物流速度">
        <el-rate v-model="form.logisticsRating" />
      </el-form-item>
      <!-- 服务态度评分 -->
      <el-form-item label="服务态度">
        <el-rate v-model="form.serviceAttitudeRating" />
      </el-form-item>
      <el-form-item label="交易感受和物品特点">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          maxlength="1000"
          show-word-limit
          placeholder="可以描述物品质量、使用感受、面交体验、交易速度等真实感受"
        />
      </el-form-item>
      <el-form-item label="物品图片或视频">
        <el-upload
          class="review-upload"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*,video/mp4,video/webm,video/quicktime"
          :on-change="handleMedia"
        >
          <el-button>上传图片或视频</el-button>
        </el-upload>
        <div v-if="mediaList.length" class="review-media-list">
          <div v-for="item in mediaList" :key="item.url" class="review-media-item">
            <img v-if="item.type === 'image'" :src="absoluteUrl(item.url)" alt="" />
            <video v-else :src="absoluteUrl(item.url)" controls />
            <el-button size="small" @click="removeMedia(item.url)">移除</el-button>
          </div>
        </div>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="anonymous">匿名评价</el-checkbox>
      </el-form-item>
      <div class="review-actions">
        <el-button @click="router.push('/orders')">返回交易</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交评价</el-button>
      </div>
    </el-form>
  </section>
</template>

<!--
  文件说明：OrderReview.vue
  作用：交易评价页，收集订单评价、评分和上传评价图片或视频。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api/modules'

const route = useRoute()
const router = useRouter()
const orderId = Number(route.params.orderId)
const order = ref(null)
const submitted = ref(false)
const submitting = ref(false)
const mediaList = ref([])
const anonymous = ref(false)
const form = reactive({
  qualityRating: 5,
  serviceRating: 5,
  logisticsRating: 5,
  serviceAttitudeRating: 5,
  content: ''
})

const mediaUrls = computed(() => JSON.stringify(mediaList.value))

const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}

const absoluteUrl = (url) => normalizeImageUrl(url)

const load = async () => {
  order.value = (await userApi.order(orderId)).data
  if (order.value.status !== 'TRADE_FINISHED') {
    ElMessage.warning('确认收货后才能评价')
    router.push('/orders')
    return
  }
  const review = (await userApi.orderReview(orderId)).data
  submitted.value = Boolean(review || order.value.isReviewed === 1)
}

const handleMedia = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  if (mediaList.value.length >= 6) {
    ElMessage.warning('最多上传6个图片或视频')
    return
  }
  const isImage = file.type.startsWith('image/')
  const isVideo = file.type.startsWith('video/')
  if (!isImage && !isVideo) {
    ElMessage.warning('请选择图片或视频文件')
    return
  }
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过50MB')
    return
  }
  const res = await userApi.uploadReviewMedia(file)
  const payload = res?.data ?? res
  const uploadedUrl = payload?.url || payload?.fileUrl || payload?.imageUrl || payload?.path
  if (!uploadedUrl) {
    ElMessage.error('图片上传失败，请稍后重试')
    return
  }
  mediaList.value.push({ url: uploadedUrl, type: isImage ? 'image' : 'video' })
  ElMessage.success('上传成功')
}

const removeMedia = (url) => {
  mediaList.value = mediaList.value.filter((item) => item.url !== url)
}

const submit = async () => {
  if (!form.qualityRating || !form.serviceRating || !form.logisticsRating || !form.serviceAttitudeRating) {
    ElMessage.warning('请完成多维度星级评分')
    return
  }
  submitting.value = true
  try {
    await userApi.createReview({
      orderId,
      ...form,
      mediaUrls: mediaUrls.value,
      anonymous: anonymous.value ? 1 : 0
    })
    ElMessage.success('评价已提交，提交后不可修改或删除')
    router.push('/orders')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
