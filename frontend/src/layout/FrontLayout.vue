<template>
  <div>
    <header class="site-header">
      <router-link class="brand" to="/">校园二手街</router-link>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/about">平台简介</router-link>
        <router-link to="/product">闲置物品</router-link>
        <router-link to="/news">校园贴士</router-link>
        <router-link to="/contact">交易咨询</router-link>
        <router-link v-if="!loggedIn && !adminLoggedIn && !merchantLoggedIn" to="/user-login">登录</router-link>
        <router-link v-if="loggedIn" to="/cart">交易清单<span v-if="cartCount" class="cart-badge">{{ cartCount
        }}</span></router-link>
        <router-link v-if="loggedIn" to="/orders">我的交易</router-link>
        <router-link v-if="loggedIn" to="/bargain-offers">我的议价</router-link>
        
        <el-dropdown v-if="loggedIn" trigger="click" @command="handleAccountCommand">
          <button class="account-trigger" type="button">
            <span class="account-name">{{ displayName }}</span>
            <span class="account-label">会员</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="address">地址管理</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown v-if="merchantLoggedIn" trigger="click" @command="handleMerchantCommand">
          <button class="account-trigger" type="button">
            <span class="account-name">{{ merchantDisplayName }}</span>
            <span class="account-label">商家</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="products">商品管理</el-dropdown-item>
              <el-dropdown-item command="orders">订单管理</el-dropdown-item>
              <el-dropdown-item command="buyer-reviews">我的评价</el-dropdown-item>
              <el-dropdown-item command="bargain-offers">议价管理</el-dropdown-item>
              <el-dropdown-item command="shop">店铺设置</el-dropdown-item>
              <el-dropdown-item command="wallet">我的钱包</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown v-if="adminLoggedIn" trigger="click" @command="handleAdminCommand">
          <button class="account-trigger" type="button">
            <span class="account-name">{{ adminDisplayName }}</span>
            <span class="account-label">管理员</span>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="admin">进入后台</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </nav>
    </header>

    <router-view />

    <footer class="site-footer">
      <div class="footer-main">
        <div class="footer-brand">
          <strong>校园二手街</strong>
          <p>面向校园学习、生活与宿舍场景的闲置物品发布、交易沟通和面交协商平台。</p>
        </div>
        <nav class="footer-links" aria-label="快速入口">
          <h2>快速入口</h2>
          <router-link to="/about">平台简介</router-link>
          <router-link to="/product">闲置物品</router-link>
          <router-link to="/news">校园贴士</router-link>
          <router-link to="/contact">交易咨询</router-link>
        </nav>
        <nav class="footer-links" aria-label="服务支持">
          <h2>服务支持</h2>
          <router-link to="/user-login">学生登录</router-link>
          <router-link to="/cart">交易清单</router-link>
          <router-link to="/orders">我的交易</router-link>
          <router-link to="/contact">交易咨询</router-link>
        </nav>
        <div class="footer-contact">
          <h2>联系信息</h2>
          <p><span>服务时间</span><strong>09:00 - 18:00</strong></p>
          <p><span>客服邮箱</span><strong>campustrade@example.com</strong></p>
          <p><span>服务承诺</span><strong>校内交易、面交、协商与评价一站式处理</strong></p>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 校园二手街 版权所有</span>
        <span>校园二手交易平台</span>
      </div>
    </footer>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px">
      <el-form label-width="82px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="changePassword">保存</el-button>
      </template>
    </el-dialog>
    <!-- 个人信息已使用独立页面 /profile 展示，移除布局内模态以保持风格一致 -->
    <el-dialog v-model="addressDialogVisible" title="地址管理" width="760px">
      <div class="address-manager">
        <el-table :data="addresses" empty-text="暂无收货地址">
          <el-table-column prop="receiverName" label="收货人" width="100" />
          <el-table-column prop="phone" label="电话" width="130" />
          <el-table-column label="地址" min-width="260">
            <template #default="{ row }">{{ addressText(row) }}</template>
          </el-table-column>
          <el-table-column label="默认" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="210">
            <template #default="{ row }">
              <el-button size="small" @click="editAddress(row)">编辑</el-button>
              <el-button v-if="row.isDefault !== 1" size="small" @click="setDefaultAddress(row.id)">设为默认</el-button>
              <el-button size="small" type="danger" plain @click="deleteAddress(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-form class="address-form" label-position="top">
          <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
          <el-form-item label="联系电话"><el-input v-model="addressForm.phone" /></el-form-item>
          <el-form-item label="省份"><el-input v-model="addressForm.province" /></el-form-item>
          <el-form-item label="城市"><el-input v-model="addressForm.city" /></el-form-item>          <el-form-item label="区县"><el-input v-model="addressForm.district" /></el-form-item>
          <el-form-item label="详细地址" class="address-detail">
            <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" />
          </el-form-item>
          <el-checkbox v-model="addressForm.isDefault" :true-label="1" :false-label="0">设为默认地址</el-checkbox>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="resetAddressForm">新增地址</el-button>
        <el-button type="primary" :loading="addressSaving" @click="saveAddress">保存地址</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<!--
  文件说明：FrontLayout.vue
  作用：前端页面或组件
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi, authApi, merchantApi, userApi } from '../api/modules'

const route = useRoute()
const router = useRouter()
const userToken = ref(localStorage.getItem('user_token') || '')
const adminToken = ref(localStorage.getItem('admin_token') || '')
const merchantToken = ref(localStorage.getItem('merchant_token') || '')
const username = ref(localStorage.getItem('user_name') || '')
const adminName = ref(localStorage.getItem('admin_name') || '')
const merchantName = ref(localStorage.getItem('merchant_name') || '')
const activePasswordRole = ref('USER')
const cartCount = ref(0)
const loggedIn = computed(() => Boolean(userToken.value))
const adminLoggedIn = computed(() => Boolean(adminToken.value))
const merchantLoggedIn = computed(() => Boolean(merchantToken.value))
const displayName = computed(() => username.value || '会员')
const adminDisplayName = computed(() => adminName.value || 'admin')
const merchantDisplayName = computed(() => merchantName.value || '商家')
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const addressDialogVisible = ref(false)
const addressSaving = ref(false)
const addresses = ref([])
const addressForm = reactive({ id: null, receiverName: '', phone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 })
// profile data is shown on dedicated /profile page; keep layout state minimal
const clearUserState = () => {
  localStorage.removeItem('user_token')
  localStorage.removeItem('user_name')
  userToken.value = ''
  username.value = ''
  cartCount.value = 0
  window.dispatchEvent(new Event('user-auth-changed'))
}

const clearAdminState = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_name')
  adminToken.value = ''
  adminName.value = ''
  window.dispatchEvent(new Event('admin-auth-changed'))
}

const loadCartCount = async () => {
  if (!localStorage.getItem('user_token')) {
    cartCount.value = 0
    return
  }
  try {
    const res = await userApi.cart()
    cartCount.value = (res.data || []).reduce((sum, item) => sum + Number(item.quantity || 1), 0)
  } catch {
    cartCount.value = 0
  }
}

const syncUserState = async () => {
  const token = localStorage.getItem('user_token')
  adminToken.value = localStorage.getItem('admin_token') || ''
  adminName.value = localStorage.getItem('admin_name') || ''
  merchantToken.value = localStorage.getItem('merchant_token') || ''
  merchantName.value = localStorage.getItem('merchant_name') || ''
  userToken.value = token || ''
  username.value = localStorage.getItem('user_name') || ''
  await loadCartCount()
  if (!token || username.value) return
  try {
    const res = await userApi.profile()
    username.value = res.data.username
    localStorage.setItem('user_name', res.data.username)
  } catch {
    username.value = ''
  }
}

const openPasswordDialog = (role) => {
  activePasswordRole.value = role
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const handleAccountCommand = async (command) => {
  if (command === 'profile') {
    // 跳转到独立的个人资料页面，统一页面风格和路由行为
    router.push('/profile')
    return
  }
  if (command === 'my-products') {
    router.push('/my-products')
    return
  }
  if (command === 'address') {
    addressDialogVisible.value = true
    resetAddressForm()
    await loadAddresses()
    return
  }
  if (command === 'password') return openPasswordDialog('USER')
  if (command === 'logout') {
    await userApi.logout().catch(() => { })
    clearUserState()
    ElMessage.success('已退出登录')
    if (route.path === '/orders') router.push('/')
  }
}

const handleMerchantCommand = async (command) => {
  if (command === 'products') return router.push('/merchant-products')
  if (command === 'orders') return router.push('/merchant-orders')
  if (command === 'buyer-reviews') return router.push('/merchant-buyer-reviews')
  if (command === 'bargain-offers') return router.push('/merchant-bargain-offers')
  if (command === 'shop') return router.push('/merchant-shop-settings')
  if (command === 'wallet') return router.push('/merchant-wallet')
  if (command === 'password') return openPasswordDialog('MERCHANT')
  if (command === 'logout') {
    await merchantApi.logout().catch(() => {})
    clearMerchantState()
    ElMessage.success('已退出登录')
    router.push('/')
  }
}

const clearMerchantState = () => {
  localStorage.removeItem('merchant_token')
  localStorage.removeItem('merchant_name')
  merchantToken.value = ''
  merchantName.value = ''
  window.dispatchEvent(new Event('merchant-auth-changed'))
}

const handleAdminCommand = async (command) => {
  if (command === 'admin') return router.push('/admin')
  if (command === 'password') return openPasswordDialog('ADMIN')
  if (command === 'logout') {
    await authApi.logout().catch(() => { })
    clearAdminState()
    ElMessage.success('已退出登录')
    if (route.path.startsWith('/admin')) router.push('/')
  }
}

const addressText = (address) => [address.province, address.city, address.district, address.detailAddress].filter(Boolean).join('')
const resetAddressForm = () => Object.assign(addressForm, { id: null, receiverName: '', phone: '', province: '', city: '', district: '', detailAddress: '', isDefault: addresses.value.length ? 0 : 1 })
const loadAddresses = async () => { addresses.value = (await userApi.addresses()).data || [] }
const editAddress = (address) => Object.assign(addressForm, { id: address.id, receiverName: address.receiverName, phone: address.phone, province: address.province || '', city: address.city || '', district: address.district || '', detailAddress: address.detailAddress, isDefault: address.isDefault })

const saveAddress = async () => {
  if (!addressForm.receiverName || !addressForm.phone || !addressForm.detailAddress) {
    ElMessage.warning('请填写收货人、电话和详细地址')
    return
  }
  addressSaving.value = true
  try {
    const payload = { ...addressForm }
    delete payload.id
    if (addressForm.id) await userApi.updateAddress(addressForm.id, payload)
    else await userApi.createAddress(payload)
    ElMessage.success('地址已保存')
    await loadAddresses()
    resetAddressForm()
  } finally {
    addressSaving.value = false
  }
}

const setDefaultAddress = async (id) => {
  await userApi.setDefaultAddress(id)
  await loadAddresses()
}

const deleteAddress = async (id) => {
  await userApi.deleteAddress(id)
  await loadAddresses()
  resetAddressForm()
}

const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请输入原密码和新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  passwordSaving.value = true
  try {
    if (activePasswordRole.value === 'ADMIN') await adminApi.changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    else if (activePasswordRole.value === 'MERCHANT') await merchantApi.changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    else await userApi.changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    passwordDialogVisible.value = false
    ElMessage.success('密码修改成功')
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  syncUserState()
  window.addEventListener('user-auth-changed', syncUserState)
  window.addEventListener('admin-auth-changed', syncUserState)
  window.addEventListener('merchant-auth-changed', syncUserState)
  window.addEventListener('cart-changed', loadCartCount)
})
onUnmounted(() => {
  window.removeEventListener('user-auth-changed', syncUserState)
  window.removeEventListener('admin-auth-changed', syncUserState)
  window.removeEventListener('merchant-auth-changed', syncUserState)
  window.removeEventListener('cart-changed', loadCartCount)
})
watch(() => route.fullPath, syncUserState)
</script>
