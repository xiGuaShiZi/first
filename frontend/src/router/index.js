/**
 * 文件说明：index.js
 * 作用：通用 JS 模块
 * 关键逻辑：
 * 1. 提供项目所需的核心逻辑、数据处理和业务交互。
 * 2. 与路由、UI 组件或 API 服务进行协作，完成页面或模块功能。
 * 3. 所有业务行为、异常处理和状态更新都应在此模块中保持清晰可维护。
 */

import { createRouter, createWebHistory } from 'vue-router'

// Use dynamic imports for route-level code-splitting to reduce initial bundle size.
const FrontLayout = () => import(/* webpackChunkName: "layout-front" */ '../layout/FrontLayout.vue')
const AdminLayout = () => import(/* webpackChunkName: "layout-admin" */ '../layout/AdminLayout.vue')

const Home = () => import(/* webpackChunkName: "home" */ '../views/front/Home.vue')
const About = () => import(/* webpackChunkName: "about" */ '../views/front/About.vue')
const Products = () => import(/* webpackChunkName: "products" */ '../views/front/Products.vue')
const ProductDetail = () => import(/* webpackChunkName: "product-detail" */ '../views/front/ProductDetail.vue')
const News = () => import(/* webpackChunkName: "news" */ '../views/front/News.vue')
const NewsDetail = () => import(/* webpackChunkName: "news-detail" */ '../views/front/NewsDetail.vue')
const Contact = () => import(/* webpackChunkName: "contact" */ '../views/front/Contact.vue')
const UserLogin = () => import(/* webpackChunkName: "user-login" */ '../views/front/UserLogin.vue')
const UserOrders = () => import(/* webpackChunkName: "user-orders" */ '../views/front/UserOrders.vue')
const UserProfile = () => import(/* webpackChunkName: "user-profile" */ '../views/front/UserProfile.vue')
const BuyerReviews = () => import(/* webpackChunkName: "buyer-reviews" */ '../views/front/BuyerReviews.vue')
const OrderReview = () => import(/* webpackChunkName: "order-review" */ '../views/front/OrderReview.vue')
const Cart = () => import(/* webpackChunkName: "cart" */ '../views/front/Cart.vue')
const MyProducts = () => import(/* webpackChunkName: "my-products" */ '../views/front/MyProducts.vue')
const MerchantProducts = () => import(/* webpackChunkName: "merchant-products" */ '../views/front/MerchantProducts.vue')
const MerchantShopSettings = () => import(/* webpackChunkName: "merchant-shop-settings" */ '../views/front/MerchantShopSettings.vue')
const MerchantWallet = () => import(/* webpackChunkName: "merchant-wallet" */ '../views/merchant/Wallet.vue')
const MerchantPublishProduct = () => import(/* webpackChunkName: "merchant-publish" */ '../views/merchant/PublishProduct.vue')
const MerchantOrders = () => import(/* webpackChunkName: "merchant-orders" */ '../views/merchant/Orders.vue')
const ShopDetail = () => import(/* webpackChunkName: "shop-detail" */ '../views/front/ShopDetail.vue')
const Login = () => import(/* webpackChunkName: "admin-login" */ '../views/admin/Login.vue')
const Dashboard = () => import(/* webpackChunkName: "admin-dashboard" */ '../views/admin/Dashboard.vue')
const AdminNews = () => import(/* webpackChunkName: "admin-news" */ '../views/admin/AdminNews.vue')
const AdminProducts = () => import(/* webpackChunkName: "admin-products" */ '../views/admin/AdminProducts.vue')
const AdminMessages = () => import(/* webpackChunkName: "admin-messages" */ '../views/admin/AdminMessages.vue')
const AdminCompany = () => import(/* webpackChunkName: "admin-company" */ '../views/admin/AdminCompany.vue')
const AdminBanners = () => import(/* webpackChunkName: "admin-banners" */ '../views/admin/AdminBanners.vue')
const AdminOrders = () => import(/* webpackChunkName: "admin-orders" */ '../views/admin/AdminOrders.vue')
const AdminReturns = () => import(/* webpackChunkName: "admin-returns" */ '../views/admin/AdminReturns.vue')
const MerchantRegister = () => import(/* webpackChunkName: "merchant-register" */ '../views/front/MerchantRegister.vue')
const MerchantLogin = () => import(/* webpackChunkName: "merchant-login" */ '../views/front/MerchantLogin.vue')
const AdminMerchants = () => import(/* webpackChunkName: "admin-merchants" */ '../views/admin/AdminMerchants.vue')
const AdminServiceFees = () => import(/* webpackChunkName: "admin-service-fees" */ '../views/admin/AdminServiceFees.vue')
const AdminCustomerRecharge = () => import(/* webpackChunkName: "admin-customer-recharge" */ '../views/admin/AdminCustomerRecharge.vue')
const AdminCustomers = () => import(/* webpackChunkName: "admin-customers" */ '../views/admin/AdminCustomers.vue')
const AdminMerchantLevelAudits = () => import(/* webpackChunkName: "admin-merchant-level-audits" */ '../views/admin/AdminMerchantLevelAudits.vue')
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: FrontLayout,
      children: [
        { path: '', component: Home },
        { path: 'about', component: About },
        { path: 'product', component: Products },
        { path: 'product/:id', component: ProductDetail },
        { path: 'news', component: News },
        { path: 'news/:id', component: NewsDetail },
        { path: 'contact', component: Contact },
        { path: 'user-login', component: UserLogin },
        { path: 'cart', component: Cart, meta: { requiresUser: true } },
        { path: 'my-products', component: MyProducts, meta: { requiresUser: true } },
        { path: 'orders', component: UserOrders, meta: { requiresUser: true } },
        { path: 'profile', component: UserProfile, meta: { requiresUser: true } },
        { path: 'buyer-reviews', component: BuyerReviews, meta: { requiresUser: true } },
        { path: 'orders/:orderId/review', component: OrderReview, meta: { requiresUser: true } },
        { path: 'merchant-register', component: MerchantRegister },
        { path: 'merchant-login', component: MerchantLogin },
        { path: 'merchant-products', component: MerchantProducts, meta: { requiresMerchant: true } },
        { path: 'merchant-shop-settings', component: MerchantShopSettings, meta: { requiresMerchant: true } },
        { path: 'merchant-wallet', component: MerchantWallet, meta: { requiresMerchant: true } },
        { path: 'merchant-orders', component: MerchantOrders, meta: { requiresMerchant: true } },
        { path: 'merchant-publish', component: MerchantPublishProduct, meta: { requiresMerchant: true } },
        { path: 'shop/:merchantId', component: ShopDetail, name: 'ShopDetail' }
      ]
    },
    { path: '/login', component: Login },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', component: Dashboard },
        { path: 'news', component: AdminNews },
        { path: 'product', component: AdminProducts },
        { path: 'message', component: AdminMessages },
        { path: 'company', component: AdminCompany },
        { path: 'banner', component: AdminBanners },
        { path: 'orders', component: AdminOrders },
        { path: 'returns', component: AdminReturns },
        { path: 'merchants', component: AdminMerchants },
        { path: 'merchant-level-audits', component: AdminMerchantLevelAudits },
        { path: 'customers', component: AdminCustomers },
        { path: 'customer-recharge', component: AdminCustomerRecharge },
        { path: 'service-fees', component: AdminServiceFees }
      ]
    },
  ]
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('admin_token')) return '/login'
  if (to.meta.requiresUser && !localStorage.getItem('user_token')) return '/user-login'
  if (to.meta.requiresMerchant && !localStorage.getItem('merchant_token')) return '/merchant-login'
})

export default router
