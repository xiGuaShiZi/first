import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  unifiedLogin: (data) => request.post('/auth/unified-login', data),
  logout: () => request.post('/auth/logout'),
  captcha: () => request.get('/auth/captcha'),
  customerRegister: (data) => request.post('/auth/customer/register', data),
  customerLogin: (data) => request.post('/auth/customer/login', data),
  merchantRegister: (data) => request.post('/auth/merchant/register', data),
  merchantLogin: (data) => request.post('/auth/merchant/login', data)
}

export const publicApi = {
  home: () => request.get('/public/home'),
  company: () => request.get('/public/company'),
  banners: () => request.get('/public/banners'),
  news: (params) => request.get('/public/news', { params }),
  newsDetail: (id) => request.get(`/public/news/${id}`),
  productCategories: () => request.get('/public/product-categories'),
  products: (params) => request.get('/public/products', { params }),
  productDetail: (id) => request.get(`/public/products/${id}`),
  productReviews: (id) => request.get(`/public/products/${id}/reviews`),
  submitMessage: (data) => request.post('/public/messages', data),
  shopDetail: (merchantId, params) => request.get(`/public/shops/${merchantId}`, { params }),
  shopReviews: (merchantId, params) => request.get(`/public/shops/${merchantId}/reviews`, { params })
}

export const adminApi = {
  dashboard: () => request.get('/admin/dashboard'),
  changePassword: (data) => request.put('/admin/password', data),
  news: (params) => request.get('/admin/news', { params }),
  createNews: (data) => request.post('/admin/news', data),
  updateNews: (id, data) => request.put(`/admin/news/${id}`, data),
  deleteNews: (id) => request.delete(`/admin/news/${id}`),
  products: (params) => request.get('/admin/products', { params }),
  createProduct: (data) => request.post('/admin/products', data),
  updateProduct: (id, data) => request.put(`/admin/products/${id}`, data),
  deleteProduct: (id) => request.delete(`/admin/products/${id}`),
  messages: (params) => request.get('/admin/messages', { params }),
  updateMessageStatus: (id, status) => request.put(`/admin/messages/${id}/status`, null, { params: { status } }),
  deleteMessage: (id) => request.delete(`/admin/messages/${id}`),
  company: () => request.get('/admin/company'),
  updateCompany: (data) => request.put('/admin/company', data),
  banners: () => request.get('/admin/banners'),
  createBanner: (data) => request.post('/admin/banners', data),
  updateBanner: (id, data) => request.put(`/admin/banners/${id}`, data),
  deleteBanner: (id) => request.delete(`/admin/banners/${id}`),
  orders: (params) => request.get('/admin/orders', { params }),
  shipOrder: (id, logisticsNo) => request.put(`/admin/orders/${id}/ship`, null, { params: { logisticsNo } }),
  orderStatistics: (params) => request.get('/admin/order-statistics', { params }),
  returns: (params) => request.get('/admin/returns', { params }),
  handleReturn: (id, data) => request.put(`/admin/returns/${id}`, data),
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/admin/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  merchants: (params) => request.get('/admin/merchants', { params }),
  auditMerchant: (id, data) => request.put(`/admin/merchants/${id}/audit`, data),
  setMerchantLevel: (id, data) => request.put(`/admin/merchants/${id}/level`, data),
  customers: (params) => request.get('/admin/customers', { params }),
  auditCustomer: (id, data) => request.put(`/admin/customers/${id}/audit`, data),
  serviceFees: (params) => request.get('/admin/service-fees', { params }),
  merchantLevelAudits: (params) => request.get('/admin/merchant-level-audits', { params }),
  feeRates: () => request.get('/admin/fee-rates')
  ,
  auditProduct: (id, data) => request.put(`/admin/products/${id}/audit`, data),
  adjustMerchantLevels: () => request.post('/admin/merchants/adjust-levels'),
  // 管理员针对用户的充值（后台工具）
  rechargeCustomer: (id, amount) => request.post(`/admin/customers/${id}/recharge`, null, { params: { amount } })
}

export const userApi = {
  profile: () => request.get('/user/profile'),
  changePassword: (data) => request.put('/user/password', data),
  logout: () => request.post('/user/logout'),
  addresses: () => request.get('/user/addresses'),
  createAddress: (data) => request.post('/user/addresses', data),
  updateAddress: (id, data) => request.put(`/user/addresses/${id}`, data),
  setDefaultAddress: (id) => request.put(`/user/addresses/${id}/default`),
  deleteAddress: (id) => request.delete(`/user/addresses/${id}`),
  publishProduct: (data) => request.post('/user/products', data),
  myProducts: (params) => request.get('/user/products', { params }),
  updateProduct: (id, data) => request.put(`/user/products/${id}`, data),
  deleteProduct: (id) => request.delete(`/user/products/${id}`),
  cart: () => request.get('/user/cart'),
  addCartItem: (data) => request.post('/user/cart', data),
  updateCartItem: (id, data) => request.put(`/user/cart/${id}`, data),
  deleteCartItem: (id) => request.delete(`/user/cart/${id}`),
  clearCart: () => request.delete('/user/cart'),
  createOrder: (data) => request.post('/user/orders', data),
  createOrders: (data) => request.post('/user/orders/batch', data),
  // 新增：一键下单并使用用户账户余额支付（后端新增的批量结算并余额支付接口）
  createAndPayOrders: (data) => request.post('/user/orders/batch/checkout', data),
  orders: (params) => request.get('/user/orders', { params }),
  order: (id) => request.get(`/user/orders/${id}`),
  payOrder: (id) => request.put(`/user/orders/${id}/pay`),
  cancelOrder: (id) => request.put(`/user/orders/${id}/cancel`),
  confirmReceipt: (id) => request.put(`/user/orders/${id}/confirm-receipt`),
  orderReview: (id) => request.get(`/user/orders/${id}/review`),
  createReview: (data) => request.post('/user/reviews', data),
  buyerReviews: (params) => request.get('/user/buyer-reviews', { params }),
  uploadReviewMedia: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/user/review-media', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  createReturn: (data) => request.post('/user/returns', data),
  returns: (params) => request.get('/user/returns', { params }),
  // 用户兑换积分接口（每100积分兑换1元，按整百兑换）
  redeemPoints: (points) => request.post('/user/points/redeem', null, { params: { points } }),
  // 议价功能
  createBargainOffer: (productId, offerPrice) => request.post('/user/bargain-offers', null, { params: { productId, offerPrice } }),
  myBargainOffers: (params) => request.get('/user/bargain-offers', { params }),
  cancelBargainOffer: (id) => request.put(`/user/bargain-offers/${id}/cancel`),
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/user/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
// 在文件最后添加
export const merchantApi = {
  profile: () => request.get('/merchant/profile'),
  changePassword: (data) => request.put('/merchant/password', data),
  logout: () => request.post('/merchant/logout'),
  upload: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/merchant/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // 店铺管理
  getShopInfo: () => request.get('/merchant/shop'),
  updateShopInfo: (data) => request.put('/merchant/shop', data),
  // 商品管理
  publishProduct: (data) => request.post('/merchant/products', data),
  myProducts: (params) => request.get('/merchant/products', { params }),
  updateProduct: (id, data) => request.put(`/merchant/products/${id}`, data),
  deleteProduct: (id) => request.delete(`/merchant/products/${id}`),
  updateProductStatus: (id, status) => request.put(`/merchant/products/${id}/status`, null, { params: { status } }),
  // 钱包管理
  getWallet: () => request.get('/merchant/wallet'),
  getWalletTransactions: (params) => request.get('/merchant/wallet/transactions', { params })
  ,
  // 订单管理（商家）
  orders: (params) => request.get('/merchant/orders', { params }),
  shipOrder: (id, logisticsNo) => request.put(`/merchant/orders/${id}/ship`, null, { params: { logisticsNo } })
  ,
  reviewBuyer: (id, rating, content) => request.post(`/merchant/orders/${id}/review-buyer`, null, { params: { rating, content } }),
  // 商家查看自己对买家的评价记录
  myBuyerReviews: (params) => request.get('/merchant/buyer-reviews', { params }),
  // 议价管理（商家）
  bargainOffers: (params) => request.get('/merchant/bargain-offers', { params }),
  handleBargainOffer: (id, action, reply) => request.put(`/merchant/bargain-offers/${id}/handle`, null, { params: { action, reply } })
}
