import type {App} from 'vue'
import {createRouter, createWebHistory} from 'vue-router'
import {staticRoutes} from './routes/staticRoutes'
import {configureNProgress} from '@/utils/router'
import {setupBeforeEachGuard} from './guards/beforeEach'
import {setupAfterEachGuard} from './guards/afterEach'

// 创建路由实例
export const router = createRouter({
  // 生产环境推荐使用 history 模式，需配合 Nginx 等服务端进行路由兜底配置
  history: createWebHistory(),
  routes: staticRoutes // 静态路由
})

// 初始化路由
export function initRouter(app: App<Element>): void {
  configureNProgress() // 顶部进度条
  setupBeforeEachGuard(router) // 路由前置守卫
  setupAfterEachGuard(router) // 路由后置守卫
  app.use(router)
}

// 主页路径，默认使用菜单第一个有效路径，配置后使用此路径
// 建议显式指定主工作台路由，避免因菜单排序变化导致首页不稳定
export const HOME_PAGE_PATH = '/dashboard/console'
