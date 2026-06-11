<template>
  <main class="login-page">
    <section class="login-header">
      <h1 class="login-title">统一登录</h1>
      <p class="login-desc">请选择您的身份，登录后进入对应功能模块</p>
    </section>

    <section class="login-cards">
      <div class="login-card" @click="goLogin('user')">
        <div class="login-card-icon user-icon">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 12c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm0 2c-3.33 0-10 1.67-10 5v2h20v-2c0-3.33-6.67-5-10-5z"/></svg>
        </div>
        <div class="login-card-body">
          <h2>普通用户</h2>
          <p>浏览闲置物品、提交订单、追踪交易</p>
          <span class="login-card-link">前往登录 &rarr;</span>
        </div>
      </div>

      <div class="login-card" @click="goLogin('merchant')">
        <div class="login-card-icon merchant-icon">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        </div>
        <div class="login-card-body">
          <h2>商家用户</h2>
          <p>管理商品上下架、处理订单、查看钱包</p>
          <span class="login-card-link">前往登录 &rarr;</span>
        </div>
      </div>

      <div class="login-card" @click="goLogin('admin')">
        <div class="login-card-icon admin-icon">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
        </div>
        <div class="login-card-body">
          <h2>系统管理员</h2>
          <p>管理后台、审核商品与商家、处理反馈</p>
          <span class="login-card-link">前往登录 &rarr;</span>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goLogin = (role) => {
  if (role === 'user') router.push('/user-login')
  else if (role === 'merchant') router.push('/merchant-login')
  else router.push('/user-login')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  background: #f5f7fa;
}

.login-header {
  text-align: center;
  margin-bottom: 48px;
}

.login-title {
  font-size: 28px;
  color: #1a1a1a;
  margin-bottom: 8px;
  font-weight: 600;
}

.login-desc {
  font-size: 15px;
  color: #8b9eb0;
}

.login-cards {
  display: flex;
  gap: 24px;
  max-width: 900px;
  width: 100%;
}

.login-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 36px 24px;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.25s;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.login-card:hover {
  box-shadow: 0 6px 24px rgba(0,0,0,0.1);
}

.login-card-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.user-icon { background: #ecf5ff; color: #409eff; }
.merchant-icon { background: #f0f9eb; color: #67c23a; }
.admin-icon { background: #fdf6ec; color: #e6a23c; }

.login-card-body h2 {
  font-size: 18px;
  color: #1a1a1a;
  margin-bottom: 8px;
  font-weight: 600;
}

.login-card-body p {
  font-size: 13px;
  color: #8b9eb0;
  line-height: 1.6;
  margin-bottom: 16px;
}

.login-card-link {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.login-card:hover .login-card-link {
  text-decoration: underline;
}
</style>
