<template>
  <div class="merchant-layout">
    <el-container>
      <el-header class="merchant-header">
        <div class="header-content">
          <h1 class="logo">商家中心</h1>
          <el-menu mode="horizontal" :default-active="activeMenu" class="nav-menu" router>
            <el-menu-item index="/merchant-products">商品管理</el-menu-item>
            <el-menu-item index="/merchant-shop-settings">店铺设置</el-menu-item>
          </el-menu>
          <div class="header-actions">
            <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <span class="username">{{ username }}</span>
                </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main class="merchant-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { merchantApi } from '../../api/modules'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

const username = computed(() => {
  const merchantInfo = localStorage.getItem('merchant_info')
  if (merchantInfo) {
    try {
      const info = JSON.parse(merchantInfo)
      return info.username || info.shopName || localStorage.getItem('merchant_name') || '商家'
    } catch {
      return localStorage.getItem('merchant_name') || '商家'
    }
  }
  return localStorage.getItem('merchant_name') || '商家'
})

const handleCommand = async (command) => {
  if (command === 'logout') {
      try {
      await merchantApi.logout()
    } finally {
      localStorage.removeItem('merchant_token')
      localStorage.removeItem('merchant_info')
      ElMessage.success('已退出登录')
      router.push('/user-login')
    }
  } else if (command === 'profile') {
    router.push('/merchant-shop-settings')
  }
}
</script>

<style scoped>
.merchant-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.merchant-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  margin: 0;
  font-size: 20px;
  color: #409eff;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.header-actions {
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  color: #606266;
}

.merchant-main {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>