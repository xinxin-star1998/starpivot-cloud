/** 自定义导航栏安全区（对齐微信胶囊，避开刘海） */

export interface CustomNavMetrics {
  /** 状态栏高度 px */
  statusBarHeight: number
  /** 导航按钮区域高度 px（与胶囊同高） */
  contentHeight: number
  /** 胶囊顶部相对状态栏的间距 px */
  contentTopGap: number
  /** 右侧需避开胶囊的宽度 px */
  menuRightGap: number
}

export function getCustomNavMetrics(): CustomNavMetrics {
  const sys = uni.getSystemInfoSync()
  const statusBarHeight = sys.statusBarHeight || 20
  const windowWidth = sys.windowWidth || 375

  let contentHeight = 32
  let contentTopGap = 6
  let menuRightGap = 100

  try {
    const menu = uni.getMenuButtonBoundingClientRect()
    if (menu?.width && menu.height) {
      contentHeight = menu.height
      contentTopGap = Math.max(0, menu.top - statusBarHeight)
      menuRightGap = Math.max(12, windowWidth - menu.left + 8)
    }
  } catch {
    // 非微信环境回退
  }

  return {
    statusBarHeight,
    contentHeight,
    contentTopGap,
    menuRightGap
  }
}
