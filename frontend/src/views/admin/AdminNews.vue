<template>
  <section class="admin-page">
    <div class="toolbar">
      <div>
        <h1>校园贴士</h1>
        <p class="admin-subtitle">维护贴士标题、封面、摘要、正文、标签和发布状态</p>
      </div>
      <el-button type="primary" @click="open()">新增贴士</el-button>
    </div>

    <div class="admin-filter news-filter">
      <el-input v-model="keyword" placeholder="搜索标题、摘要、正文、作者、来源或标签" clearable @clear="search" @keyup.enter="search" />
      <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="search">
        <el-option label="已发布" :value="1" />
        <el-option label="已下线" :value="0" />
      </el-select>
      <el-button @click="search">搜索</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column label="封面" width="110">
        <template #default="{ row }">
          <img v-if="row.coverImage" class="table-thumb" :src="imageSrc(row.coverImage)" alt="" />
          <span v-else class="muted-text">无封面</span>
        </template>
      </el-table-column>
      <el-table-column label="贴士信息" min-width="280">
        <template #default="{ row }">
          <div class="news-cell-title">{{ row.title }}</div>
          <div class="news-cell-summary">{{ row.summary || excerpt(row.content, 64) || '未填写摘要' }}</div>
          <div class="news-cell-meta">
            <span>{{ row.author || '管理员' }}</span>
            <span>{{ row.source || '校园二手街' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="标签" width="180">
        <template #default="{ row }">
          <div class="admin-tag-list">
            <el-tag v-for="tag in splitTags(row.tags)" :key="tag" size="small">{{ tag }}</el-tag>
            <span v-if="!splitTags(row.tags).length" class="muted-text">未设置</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读量" width="90" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '发布' : '下线' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="150">
        <template #default="{ row }">{{ formatDate(row.updateTime || row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button @click="preview(row)">预览</el-button>
          <el-button @click="open(row)">编辑</el-button>
          <el-button @click="toggle(row)">{{ row.status === 1 ? '下线' : '发布' }}</el-button>
          <el-button type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="admin-pagination">
      <span>共 {{ total }} 条贴士</span>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="sizes, prev, pager, next"
        background
        @size-change="search"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="visible" :title="editingId ? '编辑贴士' : '新增贴士'" width="820px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model.trim="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="240" show-word-limit />
        </el-form-item>
        <div class="form-grid-2">
          <el-form-item label="作者">
            <el-input v-model.trim="form.author" placeholder="管理员" />
          </el-form-item>
          <el-form-item label="来源">
            <el-input v-model.trim="form.source" placeholder="校园二手街" />
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model.trim="form.tags" placeholder="多个标签用逗号分隔" />
          </el-form-item>
          <el-form-item label="阅读量">
            <el-input-number v-model="form.viewCount" :min="0" />
          </el-form-item>
          <el-form-item label="排序值">
            <el-input-number v-model="form.sort" :min="0" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="下线" />
          </el-form-item>
        </div>
        <el-form-item label="封面图">
          <ImageUpload v-model="form.coverImage" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            maxlength="20000"
            show-word-limit
            placeholder="支持 HTML 图文内容，例如 <p>正文</p><figure><img src='图片地址'><figcaption>图注</figcaption></figure>"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button @click="preview(form)">预览</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="贴士预览" width="820px">
      <article class="news-preview">
        <h2>{{ previewItem.title || '未填写标题' }}</h2>
        <p class="news-preview-meta">
          {{ previewItem.author || '管理员' }} · {{ previewItem.source || '校园二手街' }} · {{ formatDate(previewItem.updateTime || previewItem.createTime) || '未保存' }}
        </p>
        <div class="admin-tag-list">
          <el-tag v-for="tag in splitTags(previewItem.tags)" :key="tag" size="small">{{ tag }}</el-tag>
        </div>
        <img v-if="previewItem.coverImage" class="news-preview-cover" :src="imageSrc(previewItem.coverImage)" alt="" />
        <p v-if="previewItem.summary" class="news-preview-summary">{{ previewItem.summary }}</p>
        <div class="article-body" v-html="previewContent"></div>
      </article>
    </el-dialog>
  </section>
</template>

<!--
  文件说明：AdminNews.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import DOMPurify from 'dompurify'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/modules'
import ImageUpload from '../../components/ImageUpload.vue'

const list = ref([])
const total = ref(0)
const keyword = ref('')
const statusFilter = ref('')
const visible = ref(false)
const previewVisible = ref(false)
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const pageSize = ref(10)
const editingId = ref(null)
const formRef = ref(null)
const emptyForm = { title: '', summary: '', author: '', source: '校园二手街', tags: '', coverImage: '', content: '', sort: 0, viewCount: 0, status: 1 }
const form = reactive({ ...emptyForm })
const previewItem = reactive({ ...emptyForm })

const rules = {
  title: [{ required: true, message: '请填写贴士标题', trigger: 'blur' }],
  content: [{ required: true, message: '请填写贴士内容', trigger: 'blur' }]
}

const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}
const imageSrc = (value) => normalizeImageUrl(value)
const previewContent = computed(() => DOMPurify.sanitize(previewItem.content || '', {
  ADD_TAGS: ['figure', 'figcaption'],
  ADD_ATTR: ['target', 'rel']
}))

const load = async () => {
  loading.value = true
  try {
    const params = { keyword: keyword.value, page: page.value, size: pageSize.value }
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const res = await adminApi.news(params)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  load()
}

const resetFilter = () => {
  keyword.value = ''
  statusFilter.value = ''
  search()
}

const open = (row) => {
  editingId.value = row?.id || null
  Object.assign(form, emptyForm, row || {})
  visible.value = true
}

const payload = () => ({
  title: form.title,
  summary: form.summary,
  author: form.author,
  source: form.source,
  tags: form.tags,
  coverImage: form.coverImage,
  content: form.content,
  sort: form.sort || 0,
  viewCount: form.viewCount || 0,
  status: form.status
})

const save = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    editingId.value ? await adminApi.updateNews(editingId.value, payload()) : await adminApi.createNews(payload())
    ElMessage.success('已保存')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggle = async (row) => {
  await adminApi.updateNews(row.id, { ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success('状态已更新')
  load()
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该贴士？', '删除确认', { type: 'warning' })
  await adminApi.deleteNews(id)
  ElMessage.success('已删除')
  load()
}

const preview = (row) => {
  Object.assign(previewItem, emptyForm, row || {})
  previewVisible.value = true
}

const splitTags = (tags) => (tags || '').split(/[,，]/).map(tag => tag.trim()).filter(Boolean)
const stripHtml = (html) => (html || '').replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
const excerpt = (html, length) => {
  const text = stripHtml(html)
  return text.length > length ? `${text.slice(0, length)}...` : text
}
const formatDate = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : ''

onMounted(load)
</script>
