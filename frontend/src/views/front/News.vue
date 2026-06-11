<template>
  <section class="page-title">
    <p>Campus Notes</p>
    <h1>校园贴士</h1>
    <span>发布二手交易经验、面交须知、使用建议和校园好物推荐。</span>
  </section>
  <section class="section">
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索贴士标题" clearable @clear="search" @keyup.enter="search" />
      <el-button type="primary" @click="search">搜索</el-button>
    </div>
    <div class="news-list">
      <router-link v-for="item in list" :key="item.id" :to="`/news/${item.id}`">
        <img v-if="item.coverImage" class="news-thumb" :src="imageSrc(item.coverImage)" alt="" />
        <time>{{ item.createTime?.slice(0, 10) }}</time>
        <h3>{{ item.title }}</h3>
        <div class="tag-row compact"><span v-for="tag in tags(item.tags)" :key="tag">{{ tag }}</span></div>
        <p>{{ item.summary || brief(item.content) }}</p>
      </router-link>
    </div>
    <el-empty v-if="loaded && !list.length" description="暂无校园贴士" />
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
  文件说明：News.vue
  作用：校园贴士列表页，展示校园经验、使用建议和交易知识。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, ref } from 'vue'
import { publicApi } from '../../api/modules'

const keyword = ref('')
const page = ref(1)
const pageSize = 8
const total = ref(0)
const list = ref([])
const loaded = ref(false)
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
const brief = (value) => value ? String(value).replace(/<[^>]*>/g, ' ').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').slice(0, 120) : ''
const load = async () => {
  const res = await publicApi.news({ keyword: keyword.value, page: page.value, size: pageSize })
  list.value = res.data?.content || []
  total.value = res.data?.totalElements || 0
  loaded.value = true
}
const search = () => { page.value = 1; load() }
const changePage = (value) => { page.value = value; load() }
onMounted(load)
</script>
