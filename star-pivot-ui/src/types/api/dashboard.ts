/**
 * 工作台首页类型定义
 */

export interface DashboardCardItem {
  des: string
  icon: string
  num: number
  change: string
}

export interface DashboardTrendData {
  xAxisData: string[]
  data: number[]
}

export interface DashboardTodoItem {
  username: string
  date: string
  complete: boolean
}

export interface DashboardDynamicItem {
  username: string
  type: string
  target: string
}

export interface DashboardNewUserItem {
  username: string
  province: string
  sex: number
  percentage: number
}

export interface ConsoleDashboardData {
  cardList: DashboardCardItem[]
  visitTrend: DashboardTrendData
  userTrend: DashboardTrendData
  todoList: DashboardTodoItem[]
  dynamicList: DashboardDynamicItem[]
  newUserList: DashboardNewUserItem[]
}
