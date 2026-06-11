<template>
  <section class="page-title">
    <p>Cart</p>
    <h1>交易清单</h1>
    <span>集中确认闲置物品数量、收货信息并提交交易。</span>
  </section>
  <section class="section cart-page">
    <el-alert v-if="!hasToken" title="请先登录学生账号后查看交易清单" type="warning" show-icon :closable="false" />

    <el-empty v-else-if="loaded && !cartItems.length" description="交易清单暂无闲置物品">
      <router-link class="back-link compact" to="/product">去看看闲置物品</router-link>
    </el-empty>

    <template v-else>
      <div class="cart-toolbar">
        <el-checkbox :model-value="allChecked" :indeterminate="partChecked" @change="toggleAll">全选</el-checkbox>
        <span>已选 {{ checkedCount }} / {{ cartItems.length }} 件</span>
      </div>

      <el-table v-loading="loading" :data="cartItems" row-key="id">
        <el-table-column label="选择" width="80">
          <template #default="{ row }">
            <el-checkbox :model-value="row.checked === 1" @change="value => updateChecked(row, value)" />
          </template>
        </el-table-column>
        <el-table-column label="物品" min-width="300">
          <template #default="{ row }">
            <div class="cart-product">
              <img :src="imageSrc(row.image, placeholder)" alt="" />
              <div>
                <strong>{{ row.productName }}</strong>
                <span>¥{{ row.price || '0.00' }}<em v-if="row.unit">/{{ row.unit }}</em></span>
                <small v-if="row.lowerPrice">较加入购物车时已降价</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="170">
          <template #default="{ row }">
            <el-input-number
              :model-value="row.quantity"
              :min="1"
              :max="row.stock || 999"
              @change="value => updateQuantity(row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="130">
          <template #default="{ row }">¥{{ subtotal(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button type="danger" plain @click="removeItem(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-checkout">
        <div class="cart-summary">
          <span>已选 {{ checkedQuantity }} 件物品</span>
          <strong>¥{{ cartTotal.toFixed(2) }}</strong>
        </div>
        <el-form label-position="top">
          <el-form-item label="收货地址">
            <el-select v-model="checkout.addressId" placeholder="请选择收货地址" style="width: 100%">
              <el-option v-for="item in addresses" :key="item.id" :label="addressLabel(item)" :value="item.id" />
            </el-select>
          </el-form-item>
          <span class="form-hint">可在顶部账号菜单中维护地址</span>
        </el-form>
        <div class="cart-actions">
          <el-button @click="clearAll">清空交易清单</el-button>
          <el-button type="success" :loading="paying" @click="submitAndPay">一键下单并使用余额支付</el-button>
          <el-button type="primary" :loading="submitting" @click="submitOrders">提交交易</el-button>
        </div>
      </div>
    </template>
  </section>
</template>

<!--
  文件说明：Cart.vue
  作用：交易清单页面，负责购物车数据加载、数量调整、勾选结算和提交订单。
  关键逻辑：
  - 负责页面展示、业务交互和状态管理。
  - 通过组件、API 和路由完成页面功能闭环。
  - 保持模板、样式和脚本职责清晰，便于维护和扩展。
-->

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api/modules'

const router = useRouter()
// 提交订单时使用的加载状态，避免重复点击。
const submitting = ref(false)
// 一键使用余额支付的加载状态
const paying = ref(false)
// 加载购物车数据的状态，用于表格区域显示 loading。
const loading = ref(false)
// 标记当前是否已经完成一次初始数据加载，便于空状态展示。
const loaded = ref(false)
// 当前用户购物车中的明细列表，包含商品、数量、选中状态等。
const cartItems = ref([])
// 用户地址列表，用于选择收货地址。
const addresses = ref([])
// 订单结算时使用的表单状态，保留当前选中的地址。
const checkout = reactive({ addressId: null })
// 购物车图片的默认占位图，避免接口返回空图时页面空白。
const placeholder = 'https://images.unsplash.com/photo-1515955656352-a1fa3ffcd111?auto=format&fit=crop&w=900&q=80'
// 判断当前是否已经登录会员，决定是否展示购物车内容。
const hasToken = computed(() => Boolean(localStorage.getItem('user_token')))
// 仅统计被勾选的购物项，便于后续批量提交订单。
const checkedItems = computed(() => cartItems.value.filter(item => item.checked !== 0))
// 已选中的购物项数量，用于展示顶部统计信息。
const checkedCount = computed(() => checkedItems.value.length)
// 已选中的商品总件数，数量累加后的结果。
const checkedQuantity = computed(() => checkedItems.value.reduce((sum, item) => sum + Number(item.quantity || 1), 0))
// 全选状态：购物车有内容且全部被选中时为 true。
const allChecked = computed(() => cartItems.value.length > 0 && checkedCount.value === cartItems.value.length)
// 半选状态：有部分被选中时为 true。
const partChecked = computed(() => checkedCount.value > 0 && checkedCount.value < cartItems.value.length)
// 已选商品的总金额，用于订单结算展示。
const cartTotal = computed(() => checkedItems.value.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 1), 0))

// 统一处理图片地址：支持后端返回的绝对地址和相对地址，避免图片路径拼接异常。
const normalizeImageUrl = (value) => {
  if (!value) return ''
  if (value.startsWith('http')) return value
  const base = import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'
  const cleanedBase = base.replace(/\/+$/, '')
  const cleanedValue = value.replace(/^\/+/, '')
  return `${cleanedBase}/${cleanedValue}`
}
// 根据当前图片值生成可渲染的图片地址，若为空则回退到占位图。
const imageSrc = (value, fallback) => normalizeImageUrl(value) || fallback

// 计算单行商品的小计金额，便于表格展示。
const subtotal = (row) => (Number(row.price || 0) * Number(row.quantity || 1)).toFixed(2)
// 拼接收货地址文本，便于下拉框中展示完整地址信息。
const addressLabel = (address) => `${address.receiverName} ${address.phone} ${[address.province, address.city, address.district, address.detailAddress].filter(Boolean).join('')}`

// 拉取当前用户的地址列表，并优先选中默认地址，保证结算时有默认收货地点。
const loadAddresses = async () => {
  addresses.value = (await userApi.addresses()).data || []
  const defaultAddress = addresses.value.find(item => item.isDefault === 1) || addresses.value[0]
  checkout.addressId = defaultAddress?.id || null
}

// 加载购物车数据并同步地址信息，完成后触发全局购物车变更事件让其他页面刷新统计。
const loadCart = async () => {
  if (!hasToken.value) return
  loading.value = true
  try {
    cartItems.value = (await userApi.cart()).data || []
    await loadAddresses()
    loaded.value = true
    window.dispatchEvent(new Event('cart-changed'))
  } finally {
    loading.value = false
  }
}

// 在本地直接同步购物车项状态，避免等待接口返回时页面出现短暂不同步。
const patchLocalItem = (id, patch) => {
  const item = cartItems.value.find(current => current.id === id)
  if (item) Object.assign(item, patch)
}

// 修改购物车数量时先更新本地状态，再调用后端接口保存，最后广播全局变更。
const updateQuantity = async (row, value) => {
  const quantity = Number(value || 1)
  patchLocalItem(row.id, { quantity })
  await userApi.updateCartItem(row.id, { quantity, selected: row.selected, checked: row.checked })
  window.dispatchEvent(new Event('cart-changed'))
}

// 切换单项选中状态时更新本地与后端状态，便于结算金额实时刷新。
const updateChecked = async (row, value) => {
  const checked = value ? 1 : 0
  patchLocalItem(row.id, { selected: checked, checked })
  await userApi.updateCartItem(row.id, { quantity: row.quantity, selected: checked, checked })
  window.dispatchEvent(new Event('cart-changed'))
}

// 全选/取消全选会批量更新所有购物项的选择状态。
const toggleAll = async (value) => {
  const checked = value ? 1 : 0
  const targets = cartItems.value.filter(item => item.checked !== checked)
  cartItems.value.forEach(item => {
    item.selected = checked
    item.checked = checked
  })
  await Promise.all(targets.map(item => userApi.updateCartItem(item.id, { quantity: item.quantity, selected: checked, checked })))
  window.dispatchEvent(new Event('cart-changed'))
}

// 删除单件交易物品，并刷新本地列表与顶部购物车统计。
const removeItem = async (id) => {
  await userApi.deleteCartItem(id)
  cartItems.value = cartItems.value.filter(item => item.id !== id)
  window.dispatchEvent(new Event('cart-changed'))
}

// 清空交易清单，通常用于用户主动放弃全部选择。
const clearAll = async () => {
  await userApi.clearCart()
  cartItems.value = []
  loaded.value = true
  window.dispatchEvent(new Event('cart-changed'))
}

// 提交交易时会先校验登录、地址和已选商品，再批量创建订单并清理购物车。
const submitOrders = async () => {
  if (!localStorage.getItem('user_token')) {
    ElMessage.warning('请先登录会员账号')
    router.push('/user-login')
    return
  }
  if (!checkout.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (!checkedItems.value.length) {
    ElMessage.warning('请选择要结算的物品')
    return
  }
  submitting.value = true
  try {
    const submittedItems = [...checkedItems.value]
    await userApi.createOrders({
      addressId: checkout.addressId,
      items: submittedItems.map(item => ({
        productId: item.productId,
        skuId: item.skuId,
        quantity: item.quantity
      }))
    })
    // 订单创建成功后，清理已提交的购物车项并更新页面状态。
    await Promise.all(submittedItems.map(item => userApi.deleteCartItem(item.id)))
    cartItems.value = cartItems.value.filter(item => !submittedItems.some(submitted => submitted.id === item.id))
    window.dispatchEvent(new Event('cart-changed'))
    ElMessage.success('交易提交成功，可在我的交易中查看')
    router.push('/orders')
  } finally {
    submitting.value = false
  }
}

// 一键下单并使用用户余额支付：调用后端新接口，若余额不足或支付失败会在页面展示错误提示
const submitAndPay = async () => {
  if (!localStorage.getItem('user_token')) {
    ElMessage.warning('请先登录会员账号')
    router.push('/user-login')
    return
  }
  if (!checkout.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (!checkedItems.value.length) {
    ElMessage.warning('请选择要结算的物品')
    return
  }
  paying.value = true
  try {
    const submittedItems = [...checkedItems.value]
    await userApi.createAndPayOrders({
      addressId: checkout.addressId,
      items: submittedItems.map(item => ({
        productId: item.productId,
        skuId: item.skuId,
        quantity: item.quantity
      }))
    })
    // 支付并创建订单成功后，清理已提交的购物车项
    await Promise.all(submittedItems.map(item => userApi.deleteCartItem(item.id)))
    cartItems.value = cartItems.value.filter(item => !submittedItems.some(submitted => submitted.id === item.id))
    window.dispatchEvent(new Event('cart-changed'))
    ElMessage.success('支付并提交交易成功，可在我的交易中查看')
    router.push('/orders')
  } catch (err) {
    ElMessage.error(err?.message || '支付失败，请稍后重试')
  } finally {
    paying.value = false
  }
}

// 页面挂载时加载购物车数据并初始化地址选择。
onMounted(loadCart)
</script>
