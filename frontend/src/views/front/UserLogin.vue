<template>
  <main class="shop-login">
    <section class="shop-login-art">
      <p>Campus Trading</p>
      <h1>登录后浏览闲置物品并完成交易</h1>
      <span>学生可以查看校园闲置物品、提交交易并查看交易记录与协商进度。</span>
    </section>
    <form class="shop-login-panel" @submit.prevent="submit">
      <el-tabs v-model="mode">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>
      <el-input v-model="form.username" placeholder="用户名" />
      <el-input v-model="form.password" type="password" placeholder="密码" show-password />
      <template v-if="mode === 'register'">
        <el-input v-model="form.phone" placeholder="联系电话" />
        <el-input v-model="form.email" placeholder="邮箱" />
        <el-select v-model="form.gender" placeholder="性别" style="width: 100%">
          <el-option label="男" value="MALE" />
          <el-option label="女" value="FEMALE" />
        </el-select>
        <el-input v-model="form.bankAccount" placeholder="银行账号（16位）" maxlength="16" />
      </template>
      <div v-else class="captcha-row">
        <el-input v-model="form.captcha" placeholder="图像验证码" />
        <button type="button" class="captcha-image" @click="loadCaptcha">
          <img v-if="captchaImage" :src="captchaImage" alt="图像验证码" />
          <span v-else>----</span>
        </button>
      </div>
      <el-button type="primary" native-type="submit" :loading="loading">
        {{ mode === 'login' ? '登录' : '注册' }}
      </el-button>
      
      <div v-if="mode === 'login'" class="other-login-options">
        <router-link to="/merchant-login" class="merchant-login-link">商家登录入口 →</router-link>
      </div>

      <div v-if="mode === 'login'" class="register-link">
        还没有商家账号？<router-link to="/merchant-register">商家注册</router-link>
      </div>
    </form>
  </main>
</template>

<!--
  文件说明：UserLogin.vue
  作用：学生登录与注册入口页，处理会员认证和验证码。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api/modules'

const router = useRouter()
const mode = ref('login')
const loading = ref(false)
const captchaId = ref('')
const captchaImage = ref('')
const form = reactive({ 
  username: '', 
  password: '', 
  phone: '', 
  email: '', 
  captcha: '', 
  gender: '', 
  bankAccount: '' 
})

const loadCaptcha = async () => {
  const res = await authApi.captcha()
  captchaId.value = res.data.captchaId
  captchaImage.value = res.data.captchaImage
  form.captcha = ''
}

const submit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  // 注册时的额外验证
  if (mode.value === 'register') {
    if (!form.phone) {
      ElMessage.warning('请输入联系电话')
      return
    }
    if (!form.gender) {
      ElMessage.warning('请选择性别')
      return
    }
    if (!form.bankAccount) {
      ElMessage.warning('请输入银行账号')
      return
    }
    if (!/^\d{16}$/.test(form.bankAccount)) {
      ElMessage.warning('银行账号必须为16位数字')
      return
    }
  }
  
  loading.value = true
  try {
    if (mode.value === 'register') {
      await authApi.customerRegister(form)
      ElMessage.success('注册成功，请登录')
      mode.value = 'login'
      // 清空注册表单
      form.phone = ''
      form.email = ''
      form.gender = ''
      form.bankAccount = ''
      await loadCaptcha()
      return
    }
    localStorage.removeItem('merchant_token')
    localStorage.removeItem('merchant_info')

    const res = await authApi.unifiedLogin({ 
      username: form.username, 
      password: form.password, 
      captcha: form.captcha, 
      captchaId: captchaId.value 
    })
    if (res.data.role === 'ADMIN') {
      localStorage.setItem('admin_token', res.data.token)
      localStorage.setItem('admin_name', res.data.username || form.username)
      window.dispatchEvent(new Event('admin-auth-changed'))
      ElMessage.success('管理员登录成功')
      router.push('/admin')
      return
    }
    localStorage.setItem('user_token', res.data.token)
    localStorage.setItem('user_name', res.data.username || form.username)
    window.dispatchEvent(new Event('user-auth-changed'))
    ElMessage.success('登录成功')
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (localStorage.getItem('admin_token')) {
    router.replace('/admin')
    return
  }
  if (localStorage.getItem('merchant_token')) {
    router.replace('/merchant-products')
    return
  }
  if (localStorage.getItem('user_token')) {
    router.replace('/orders')
    return
  }
  loadCaptcha()
})
</script>

<style scoped>
.shop-login {
  display: flex;
  gap: 48px;
  padding: 60px 40px;
  max-width: 1200px;
  margin: 0 auto;
  align-items: flex-start;
}

.shop-login-art {
  flex: 1;
  padding: 40px;
}

.shop-login-art p {
  font-size: 14px;
  color: #8b9eb0;
  margin-bottom: 16px;
}

.shop-login-art h1 {
  font-size: 32px;
  margin-bottom: 16px;
  color: #1a1a1a;
}

.shop-login-art span {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}

.shop-login-panel {
  flex: 0 0 480px;
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.shop-login-panel :deep(.el-tabs) {
  margin-bottom: 20px;
}

.shop-login-panel :deep(.el-input) {
  margin-bottom: 16px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.captcha-row :deep(.el-input) {
  flex: 1;
  margin-bottom: 0;
}

.captcha-image {
  width: 120px;
  height: 42px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.other-login-options {
  margin-top: 16px;
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.merchant-login-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.merchant-login-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.merchant-login-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.register-link {
  margin-top: 12px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>