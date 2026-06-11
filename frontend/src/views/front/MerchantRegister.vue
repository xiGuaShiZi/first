<template>
  <main class="merchant-register">
    <section class="merchant-register-art">
      <p>Merchant Registration</p>
      <h1>商家入驻申请</h1>
      <span>提交营业执照和身份证明，审核通过后即可发布商品</span>
    </section>
    
    <form class="merchant-register-panel" @submit.prevent="submit">
      <h2>商家入驻申请</h2>
      
      <el-input v-model="form.username" placeholder="用户名" maxlength="80" />
      <el-input v-model="form.password" type="password" placeholder="登录密码（6-40位）" show-password />
      <el-input v-model="form.realName" placeholder="真实姓名" maxlength="50" />
      
      <el-select v-model="form.gender" placeholder="性别" style="width: 100%">
        <el-option label="男" value="MALE" />
        <el-option label="女" value="FEMALE" />
      </el-select>
      
      <el-input v-model="form.phone" placeholder="手机号" maxlength="20" />
      <el-input v-model="form.idCard" placeholder="身份证号（18位）" maxlength="18" />
      <el-input v-model="form.bankAccount" placeholder="银行账号（10-30位数字）" maxlength="30" />
      
      <div class="upload-section">
        <label>身份证照片</label>
        <el-upload
          :show-file-list="false"
          :before-upload="handleIdCardUpload"
          accept="image/*"
        >
          <el-button type="primary" :loading="uploadingIdCard">
            {{ idCardImage ? '已上传，点击更换' : '上传身份证' }}
          </el-button>
        </el-upload>
        <el-image v-if="idCardImage" :src="idCardImage" fit="cover" class="preview-image" />
      </div>
      
      <div class="upload-section">
        <label>营业执照</label>
        <el-upload
          :show-file-list="false"
          :before-upload="handleLicenseUpload"
          accept="image/*"
        >
          <el-button type="primary" :loading="uploadingLicense">
            {{ businessLicense ? '已上传，点击更换' : '上传营业执照' }}
          </el-button>
        </el-upload>
        <el-image v-if="businessLicense" :src="businessLicense" fit="cover" class="preview-image" />
      </div>
      
      <el-button type="primary" native-type="submit" :loading="loading" size="large">
        提交申请
      </el-button>
      
      <div class="login-link">
        已有账号？<router-link to="/user-login">立即登录</router-link>
      </div>
    </form>
  </main>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi, merchantApi } from '../../api/modules'

const router = useRouter()
const loading = ref(false)
const uploadingIdCard = ref(false)
const uploadingLicense = ref(false)
const idCardImage = ref('')
const businessLicense = ref('')

const form = reactive({
  username: '',
  password: '',
  realName: '',
  gender: 'MALE',
  phone: '',
  idCard: '',
  bankAccount: ''
})

const handleIdCardUpload = async (file) => {
  uploadingIdCard.value = true
  try {
    const res = await merchantApi.upload(file)
    idCardImage.value = res.data.url
    ElMessage.success('身份证上传成功')
  } catch (error) {
    ElMessage.error('身份证上传失败')
  } finally {
    uploadingIdCard.value = false
  }
  return false
}

const handleLicenseUpload = async (file) => {
  uploadingLicense.value = true
  try {
    const res = await merchantApi.upload(file)
    businessLicense.value = res.data.url
    ElMessage.success('营业执照上传成功')
  } catch (error) {
    ElMessage.error('营业执照上传失败')
  } finally {
    uploadingLicense.value = false
  }
  return false
}

const submit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (!form.realName) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!form.gender) {
    ElMessage.warning('请选择性别')
    return
  }
  if (!form.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!form.idCard || form.idCard.length !== 18) {
    ElMessage.warning('请输入正确的身份证号（18位）')
    return
  }
  if (!form.bankAccount || !/^\d{10,30}$/.test(form.bankAccount)) {
    ElMessage.warning('银行账号必须为10-30位数字')
    return
  }
  if (!idCardImage.value) {
    ElMessage.warning('请上传身份证照片')
    return
  }
  if (!businessLicense.value) {
    ElMessage.warning('请上传营业执照')
    return
  }
  
  loading.value = true
  try {
    await authApi.merchantRegister({
      ...form,
      idCardImage: idCardImage.value,
      businessLicense: businessLicense.value
    })
    ElMessage.success('注册成功！请等待管理员审核，审核通过后可登录')
    router.push('/user-login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.merchant-register {
  display: flex;
  gap: 48px;
  padding: 60px 40px;
  max-width: 1200px;
  margin: 0 auto;
  align-items: flex-start;
}

.merchant-register-art {
  flex: 1;
  padding: 40px;
}

.merchant-register-art p {
  font-size: 14px;
  color: #8b9eb0;
  margin-bottom: 16px;
}

.merchant-register-art h1 {
  font-size: 32px;
  margin-bottom: 16px;
  color: #1a1a1a;
}

.merchant-register-art span {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}

.merchant-register-panel {
  flex: 0 0 480px;
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.merchant-register-panel h2 {
  font-size: 24px;
  margin-bottom: 24px;
  color: #1a1a1a;
}

.merchant-register-panel :deep(.el-input) {
  margin-bottom: 16px;
}

.merchant-register-panel :deep(.el-select) {
  margin-bottom: 16px;
}

.upload-section {
  margin-bottom: 20px;
}

.upload-section label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.preview-image {
  margin-top: 12px;
  width: 200px;
  height: 150px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  color: #666;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}
</style>