<template>
  <main class="merchant-login">
    <section class="merchant-login-art">
      <p>Merchant Login</p>
      <h1>商家登录</h1>
      <span>审核通过后可登录商家后台，管理商品和订单</span>
    </section>
    
    <form class="merchant-login-panel" @submit.prevent="submit">
      <h2>商家登录</h2>
      
      <el-input v-model="form.username" placeholder="用户名" />
      <el-input v-model="form.password" type="password" placeholder="密码" show-password />
      
      <div class="captcha-row">
        <el-input v-model="form.captcha" placeholder="图像验证码" />
        <button type="button" class="captcha-image" @click="loadCaptcha">
          <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
          <span v-else>----</span>
        </button>
      </div>
      
      <el-button type="primary" native-type="submit" :loading="loading" size="large">
        登录
      </el-button>
      
      <div class="register-link">
        还没有账号？<router-link to="/merchant-register">立即注册</router-link>
      </div>
      <div class="other-option">
        <el-button type="text" @click="goToUser">返回普通用户界面</el-button>
      </div>
    </form>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api/modules'

const router = useRouter()
const loading = ref(false)
const captchaId = ref('')
const captchaImage = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: ''
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
  if (!form.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  loading.value = true
  try {
    const res = await authApi.merchantLogin({
      ...form,
      captchaId: captchaId.value
    })
    localStorage.setItem('merchant_token', res.data.token)
    localStorage.setItem('merchant_name', form.username)
    localStorage.setItem('merchant_info', JSON.stringify({ username: form.username, shopName: form.username }))
    window.dispatchEvent(new Event('merchant-auth-changed'))
    ElMessage.success('登录成功')
    router.push('/merchant-products')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (localStorage.getItem('merchant_token')) {
    router.replace('/merchant-products')
    return
  }
  loadCaptcha()
})

const goToUser = () => {
  // Prefer going back if there is a history entry; otherwise navigate to home
  if (window.history.length > 1) router.back(); else router.push('/')
}
</script>

<style scoped>
.merchant-login {
  display: flex;
  gap: 48px;
  padding: 60px 40px;
  max-width: 1200px;
  margin: 0 auto;
  align-items: flex-start;
}

.merchant-login-art {
  flex: 1;
  padding: 40px;
}

.merchant-login-art p {
  font-size: 14px;
  color: #8b9eb0;
  margin-bottom: 16px;
}

.merchant-login-art h1 {
  font-size: 32px;
  margin-bottom: 16px;
  color: #1a1a1a;
}

.merchant-login-art span {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}

.merchant-login-panel {
  flex: 0 0 480px;
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.merchant-login-panel h2 {
  font-size: 24px;
  margin-bottom: 24px;
  color: #1a1a1a;
}

.merchant-login-panel :deep(.el-input) {
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

.register-link {
  margin-top: 20px;
  text-align: center;
  color: #666;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
}

.other-option { text-align: center; margin-top: 8px }
.other-option .el-button { color: #409eff }
</style>