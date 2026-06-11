<template>
  <div class="admin-customer-recharge">
    <h2>用户充值（后台工具）</h2>
    <div>
      <label>客户ID: <input v-model.number="customerId" type="number" /></label>
    </div>
    <div>
      <label>充值金额(元): <input v-model.number="amount" type="number" step="0.01" min="0.01" /></label>
    </div>
    <div>
      <button @click="onRecharge" :disabled="loading">充值</button>
    </div>
    <div v-if="message" class="message">{{ message }}</div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { adminApi } from '../../api/modules'

export default {
  setup() {
    const customerId = ref(null)
    const amount = ref(0.00)
    const loading = ref(false)
    const message = ref('')

    const onRecharge = async () => {
      message.value = ''
      if (!customerId.value) { message.value = '请输入客户ID'; return }
      if (!amount.value || amount.value <= 0) { message.value = '请输入大于0的金额'; return }
      loading.value = true
      try {
        const res = await adminApi.rechargeCustomer(customerId.value, amount.value)
        message.value = '充值成功：余额 ' + (res.data.balance ?? res.balance)
      } catch (err) {
        message.value = err?.response?.data?.message || '充值失败'
      } finally { loading.value = false }
    }

    return { customerId, amount, onRecharge, loading, message }
  }
}
</script>

<style scoped>
.message { margin-top: 8px; color: #2c7 }
</style>

