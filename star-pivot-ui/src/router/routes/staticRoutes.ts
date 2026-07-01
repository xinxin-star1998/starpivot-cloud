import { AppRouteRecordRaw } from '@/utils/router'

/**
 * 静态路由配置（不需要权限就能访问的路由）
 *
 * 属性说明：
 * isHideTab: true 表示不在标签页中显示
 *
 * 注意事项：
 * 1、path、name 不要和动态路由冲突，否则会导致路由冲突无法访问
 * 2、静态路由不管是否登录都可以访问
 */
export const staticRoutes: AppRouteRecordRaw[] = [
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@views/auth/login/index.vue'),
    meta: { title: 'menus.login.title', isHideTab: true }
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('@views/auth/register/index.vue'),
    meta: { title: 'menus.register.title', isHideTab: true }
  },
  {
    path: '/auth/forget-password',
    name: 'ForgetPassword',
    component: () => import('@views/auth/forget-password/index.vue'),
    meta: { title: 'menus.forgetPassword.title', isHideTab: true }
  },
  {
    path: '/privacy-policy',
    name: 'PrivacyPolicy',
    component: () => import('@views/auth/privacy-policy/index.vue'),
    meta: { title: 'menus.privacyPolicy.title', isHideTab: true }
  },
  {
    path: '/403',
    name: 'Exception403',
    component: () => import('@views/exception/403/index.vue'),
    meta: { title: '403', isHideTab: true }
  },
  {
    path: '/500',
    name: 'Exception500',
    component: () => import('@views/exception/500/index.vue'),
    meta: { title: '500', isHideTab: true }
  },
  {
    path: '/result/success',
    name: 'ResultSuccess',
    component: () => import('@views/result/success/index.vue'),
    meta: { title: '操作成功', isHideTab: true }
  },
  {
    path: '/result/fail',
    name: 'ResultFail',
    component: () => import('@views/result/fail/index.vue'),
    meta: { title: '操作失败', isHideTab: true }
  },
  {
    path: '/outside',
    component: () => import('@views/index/index.vue'),
    name: 'Outside',
    meta: { title: 'menus.outside.title' },
    children: [
      // iframe 内嵌页面
      {
        path: '/outside/iframe/:path',
        name: 'Iframe',
        component: () => import('@/views/outside/Iframe.vue'),
        meta: { title: 'iframe' }
      }
    ]
  },
  {
    path: '/portal',
    component: () => import('@views/portal/layout/index.vue'),
    meta: { isFullPage: true, isHideTab: true },
    children: [
      {
        path: '',
        name: 'PortalHome',
        component: () => import('@views/portal/home/index.vue'),
        meta: { title: '商城首页', isHideTab: true }
      },
      {
        path: 'product/:id',
        name: 'PortalProductDetail',
        component: () => import('@views/portal/product/detail.vue'),
        meta: { title: '商品详情', isHideTab: true }
      },
      {
        path: 'cart',
        name: 'PortalCart',
        component: () => import('@views/portal/cart/index.vue'),
        meta: { title: '购物车', isHideTab: true }
      },
      {
        path: 'checkout',
        name: 'PortalCheckout',
        component: () => import('@views/portal/checkout/index.vue'),
        meta: { title: '确认订单', isHideTab: true }
      },
      {
        path: 'orders',
        name: 'PortalOrders',
        component: () => import('@views/portal/orders/index.vue'),
        meta: { title: '我的订单', isHideTab: true }
      },
      {
        path: 'coupons',
        name: 'PortalCoupons',
        component: () => import('@views/portal/coupons/index.vue'),
        meta: { title: '优惠券', isHideTab: true }
      },
      {
        path: 'search',
        name: 'PortalSearch',
        component: () => import('@views/portal/search/index.vue'),
        meta: { title: '商品搜索', isHideTab: true }
      },
      {
        path: 'subject/:id',
        name: 'PortalSubject',
        component: () => import('@views/portal/subject/index.vue'),
        meta: { title: '专题活动', isHideTab: true }
      },
      {
        path: 'seckill',
        name: 'PortalSeckill',
        component: () => import('@views/portal/seckill/index.vue'),
        meta: { title: '限时秒杀', isHideTab: true }
      },
      {
        path: 'account',
        name: 'PortalAccount',
        component: () => import('@views/portal/account/index.vue'),
        meta: { title: '会员中心', isHideTab: true }
      },
      {
        path: 'account/favorites',
        name: 'PortalFavorites',
        component: () => import('@views/portal/account/favorites/index.vue'),
        meta: { title: '我的收藏', isHideTab: true }
      },
      {
        path: 'account/reviews',
        name: 'PortalReviews',
        component: () => import('@views/portal/account/reviews/index.vue'),
        meta: { title: '我的评价', isHideTab: true }
      },
      {
        path: 'account/pending-reviews',
        name: 'PortalPendingReviews',
        component: () => import('@views/portal/account/pending-reviews/index.vue'),
        meta: { title: '待评价', isHideTab: true }
      },
      {
        path: 'account/addresses',
        name: 'PortalAddresses',
        component: () => import('@views/portal/account/addresses/index.vue'),
        meta: { title: '收货地址', isHideTab: true }
      },
      {
        path: 'account/history',
        name: 'PortalBrowseHistory',
        component: () => import('@views/portal/account/history/index.vue'),
        meta: { title: '浏览足迹', isHideTab: true }
      },
      {
        path: 'login',
        name: 'PortalLogin',
        component: () => import('@views/portal/auth/login.vue'),
        meta: { title: '会员登录', isHideTab: true }
      },
      {
        path: 'register',
        name: 'PortalRegister',
        component: () => import('@views/portal/auth/register.vue'),
        meta: { title: '会员注册', isHideTab: true }
      },
      {
        path: 'login/wechat/callback',
        name: 'PortalWechatCallback',
        component: () => import('@views/portal/auth/wechat-callback.vue'),
        meta: { title: '微信登录', isHideTab: true }
      },
      {
        path: 'account/security',
        name: 'PortalAccountSecurity',
        component: () => import('@views/portal/account/security/index.vue'),
        meta: { title: '账号安全', isHideTab: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'Exception404',
    component: () => import('@views/exception/404/index.vue'),
    meta: { title: '404', isHideTab: true }
  }
]
