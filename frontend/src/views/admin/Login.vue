<template>
  <main class="login-page">
    <section class="login-panel">
      <p>Unified Login</p>
      <h1>统一登录</h1>
      <span>管理员和会员均通过统一入口登录，验证成功后按角色进入对应功能。</span>
      <el-button type="primary" @click="router.push('/user-login')">前往登录</el-button>
    </section>
  </main>
</template>

<!--
  文件说明：Login.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>
