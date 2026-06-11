<template>
  <section class="page-title detail-title">
    <p>{{ news.createTime?.slice(0, 10) || 'Campus Note' }}</p>
    <h1>{{ news.title || '校园贴士详情' }}</h1>
    <span>作者：{{ news.author || '管理员' }} · 来源：{{ news.source || '校园二手街' }} · 阅读 {{ news.viewCount || 0 }}</span>
    <div class="tag-row"><span v-for="tag in tags(news.tags)" :key="tag">{{ tag }}</span></div>
  </section>
  <section class="section detail-content">
    <img v-if="news.coverImage" class="article-cover" :src="imageSrc(news.coverImage)" alt="" />
    <article class="article-body" v-html="safeContent"></article>
    <router-link class="back-link" to="/news">返回校园贴士</router-link>
  </section>
</template>

<!--
  文件说明：NewsDetail.vue
  作用：校园贴士详情页，展示单篇贴士正文和封面。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import DOMPurify from 'dompurify'
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { publicApi } from '../../api/modules'

const route = useRoute()
const news = ref({})
const tags = (value) => value ? String(value).split(',').map(item => item.trim()).filter(Boolean) : []
const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}
const imageSrc = (value) => normalizeImageUrl(value)
const safeContent = computed(() => DOMPurify.sanitize(news.value.content || '', {
  ADD_TAGS: ['figure', 'figcaption'],
  ADD_ATTR: ['target', 'rel']
}))

onMounted(async () => {
  news.value = (await publicApi.newsDetail(route.params.id)).data || {}
})
</script>
