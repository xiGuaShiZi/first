<template>
  <section class="page-title">
    <p>Platform</p>
    <h1>平台简介</h1>
    <span>围绕校园学习、生活和宿舍场景，帮助学生快速发布闲置物品、寻找好物并完成交易。</span>
  </section>
  <section class="section two-col">
    <div>
      <p class="eyebrow">Profile</p>
      <h2>{{ company.companyName || '校园二手街' }}</h2>
    </div>
    <div class="prose">
      <p>{{ company.intro || '校园二手街是一家面向校园场景的闲置物品交易平台，聚焦学习用品、数码设备、衣物、生活好物等二手资源流转。' }}</p>
      <p>{{ company.culture || '轻量发布、清晰价格、优先面交与协商，帮助学生更省心地完成校园内物品交易。' }}</p>
    </div>
  </section>
  <section class="section capability-grid">
    <div>
      <h3>闲置物品</h3>
      <p>物品信息包含封面、分类、标签、价格和状态，方便学生快速判断是否适合交易。</p>
    </div>
    <div>
      <h3>学生交易</h3>
      <p>学生登录后可浏览闲置物品、提交交易单并确认收货与取件方式。</p>
    </div>
    <div>
      <h3>交易协商</h3>
      <p>用户可基于交易单发起面交、换货、退款或其他协商，平台统一处理沟通。</p>
    </div>
  </section>
</template>

<!--
  文件说明：About.vue
  作用：平台简介页面，展示校园二手交易平台的定位与价值。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, ref } from 'vue'
import { publicApi } from '../../api/modules'

const company = ref({})
onMounted(async () => {
  company.value = (await publicApi.company()).data || {}
})
</script>
