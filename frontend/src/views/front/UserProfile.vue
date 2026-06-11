<template>
  <div class="user-profile">
    <el-card>
      <div class="header-row">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <router-link to="/">首页</router-link>
            </el-breadcrumb-item>
            <el-breadcrumb-item>个人中心</el-breadcrumb-item>
          </el-breadcrumb>
          <el-button type="text" icon="el-icon-arrow-left" @click="goBack">返回</el-button>
        </div>
        <div class="header-actions">
          <h2>我的资料</h2>
          <router-link to="/buyer-reviews">查看我的商家评价</router-link>
        </div>
      </div>

      <div v-if="loading">加载中...</div>
      <div v-else>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ profile.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="账户余额">¥{{ profile.balance ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="积分">{{ profile.points ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="商家评价次数">{{ profile.buyerMerchantReviewCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="商家好评率">{{ profile.buyerMerchantPositiveRate ?? 0 }}%</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h3>积分兑换（每100积分 = ¥1，按整百兑换）</h3>
        <el-form :inline="true" class="redeem-form">
          <el-form-item>
            <el-input-number v-model="redeemAmount" :min="100" :step="100" :controls="false" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="redeemLoading" @click="onRedeem">兑换</el-button>
          </el-form-item>
        </el-form>
        <el-alert v-if="message" :title="message" type="success" show-icon closable @close="message = ''" />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../../api/modules'

export default {
  setup() {
    const profile = ref({})
    const loading = ref(true)
    const redeemAmount = ref(100)
    const redeemLoading = ref(false)
    const message = ref('')

    const router = useRouter()

    const load = async () => {
      loading.value = true
      try {
        const res = await userApi.profile()
        profile.value = res.data || res
      } catch (e) {
        console.error(e)
      } finally {
        loading.value = false
      }
    }

    const onRedeem = async () => {
      message.value = ''
      if (!redeemAmount.value || redeemAmount.value < 100) {
        message.value = '最少兑换100积分，且按整百输入'
        return
      }
      redeemLoading.value = true
      try {
        await userApi.redeemPoints(redeemAmount.value)
        message.value = '兑换成功'
        await load()
      } catch (err) {
        message.value = err?.response?.data?.message || '兑换失败'
      } finally {
        redeemLoading.value = false
      }
    }

    const goBack = () => {
      // prefer router.back(); fallback to home
      if (window.history.length > 1) router.back(); else router.push('/')
    }

    onMounted(load)
    return { profile, loading, redeemAmount, onRedeem, redeemLoading, message, goBack }
  }
}
</script>

<style scoped>
.user-profile { padding: 16px }
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px }
.header-left { display: flex; align-items: center; gap: 8px }
.header-actions { display: flex; align-items: center; gap: 12px }
.message { margin-top: 8px; color: #2c7 }
</style>

