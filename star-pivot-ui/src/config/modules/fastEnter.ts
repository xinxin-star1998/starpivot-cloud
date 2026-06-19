/**
 * 快速入口配置
 * 包含：应用列表、快速链接等配置
 */
import type { FastEnterConfig } from '@/types/config'

const fastEnterConfig: FastEnterConfig = {
  // 显示条件（屏幕宽度）
  minWidth: 1200,
  // 应用列表
  applications: [
    {
      name: '工作台',
      description: '系统概览与数据统计',
      icon: 'ri:pie-chart-line',
      iconColor: '#377dff',
      enabled: true,
      order: 1,
      routeName: 'Console'
    },
    {
      name: '其他前端项目',
      description: '跳转到其他前端项目',
      icon: 'ri:external-link-line',
      iconColor: '#13DEB9',
      enabled: true,
      order: 2,
      link: 'https://your-frontend-project.com' // 替换为你的前端项目地址
    }
  ],
  // 快速链接
  quickLinks: []
}

export default Object.freeze(fastEnterConfig)
