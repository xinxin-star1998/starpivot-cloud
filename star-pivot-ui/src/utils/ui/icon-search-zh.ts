/** 中文关键词 → Iconify 图标名常用英文检索词（可多项，命中任一即算匹配） */
const ICON_SEARCH_ZH_MAP: Record<string, string[]> = {
  首页: ['home', 'house'],
  主页: ['home'],
  房子: ['home', 'house', 'building'],
  用户: ['user', 'account', 'person', 'people'],
  会员: ['user', 'vip', 'member'],
  账户: ['user', 'account'],
  账号: ['user', 'account'],
  密码: ['lock', 'password', 'key'],
  登录: ['login', 'sign-in', 'enter'],
  登出: ['logout', 'sign-out', 'exit'],
  退出: ['logout', 'exit', 'leave'],
  注册: ['register', 'signup', 'user-add'],
  设置: ['setting', 'config', 'cog', 'gear', 'adjust'],
  配置: ['setting', 'config', 'cog'],
  系统: ['setting', 'computer', 'server', 'chip'],
  菜单: ['menu', 'bars', 'list'],
  导航: ['menu', 'compass', 'map'],
  搜索: ['search', 'magnify', 'find'],
  查询: ['search', 'filter'],
  筛选: ['filter', 'funnel'],
  排序: ['sort', 'arrow'],
  添加: ['add', 'plus', 'create'],
  新增: ['add', 'plus'],
  创建: ['add', 'create', 'plus'],
  编辑: ['edit', 'pencil', 'pen'],
  修改: ['edit', 'pencil'],
  删除: ['delete', 'trash', 'remove', 'bin'],
  移除: ['remove', 'delete', 'minus'],
  保存: ['save', 'disk', 'check'],
  取消: ['cancel', 'close', 'x'],
  关闭: ['close', 'x', 'times'],
  确认: ['check', 'confirm', 'tick'],
  提交: ['check', 'send', 'upload'],
  上传: ['upload', 'cloud-upload', 'arrow-up'],
  下载: ['download', 'cloud-download', 'arrow-down'],
  导入: ['import', 'upload', 'login'],
  导出: ['export', 'download'],
  刷新: ['refresh', 'reload', 'sync'],
  同步: ['sync', 'refresh', 'cloud'],
  复制: ['copy', 'duplicate', 'clipboard'],
  粘贴: ['paste', 'clipboard'],
  分享: ['share', 'export'],
  链接: ['link', 'chain', 'url'],
  图片: ['image', 'photo', 'picture', 'camera'],
  相册: ['image', 'photo', 'album'],
  相机: ['camera', 'photo'],
  视频: ['video', 'play', 'film'],
  音频: ['audio', 'music', 'volume'],
  音乐: ['music', 'note'],
  文件: ['file', 'document', 'folder'],
  文档: ['document', 'file', 'text'],
  文件夹: ['folder', 'directory'],
  附件: ['paperclip', 'attachment', 'link'],
  邮件: ['mail', 'email', 'envelope'],
  邮箱: ['mail', 'email'],
  消息: ['message', 'chat', 'comment', 'bell'],
  通知: ['bell', 'notification', 'alert'],
  提醒: ['bell', 'alarm', 'alert'],
  公告: ['bell', 'megaphone', 'speaker'],
  评论: ['comment', 'chat', 'message'],
  聊天: ['chat', 'message', 'comment'],
  电话: ['phone', 'call', 'telephone'],
  手机: ['device-mobile', 'phone', 'smartphone'],
  电脑: ['computer', 'desktop', 'laptop'],
  平板: ['tablet', 'device'],
  打印: ['printer', 'print'],
  扫描: ['scan', 'qrcode'],
  二维码: ['qrcode', 'scan'],
  购物车: ['cart', 'shopping'],
  购物: ['cart', 'shopping', 'bag'],
  订单: ['order', 'receipt', 'clipboard'],
  商品: ['product', 'box', 'cube', 'tag'],
  产品: ['product', 'cube', 'box'],
  库存: ['warehouse', 'archive', 'box', 'inventory'],
  仓库: ['warehouse', 'building', 'archive'],
  物流: ['truck', 'shipping', 'delivery'],
  配送: ['truck', 'delivery', 'shipping'],
  支付: ['payment', 'credit-card', 'wallet', 'currency'],
  钱包: ['wallet', 'money', 'currency'],
  金额: ['currency', 'dollar', 'money', 'banknotes'],
  价格: ['currency', 'tag', 'receipt'],
  优惠: ['ticket', 'gift', 'percent', 'tag'],
  优惠券: ['ticket', 'gift'],
  礼物: ['gift', 'present'],
  统计: ['chart', 'graph', 'analytics', 'presentation'],
  图表: ['chart', 'graph', 'presentation'],
  报表: ['chart', 'document', 'table'],
  数据: ['database', 'chart', 'table', 'circle-stack'],
  数据库: ['database', 'server'],
  表格: ['table', 'grid', 'view-columns'],
  列表: ['list', 'bars', 'queue'],
  日历: ['calendar', 'date'],
  时间: ['clock', 'time', 'calendar'],
  日期: ['calendar', 'date'],
  时钟: ['clock', 'time'],
  位置: ['map', 'location', 'pin', 'marker'],
  地图: ['map', 'location', 'globe'],
  定位: ['location', 'map-pin', 'marker'],
  地址: ['map', 'location', 'home'],
  公司: ['building', 'office', 'briefcase'],
  部门: ['building', 'users', 'user-group'],
  团队: ['users', 'user-group', 'people'],
  角色: ['shield', 'key', 'user-circle'],
  权限: ['shield', 'lock', 'key'],
  安全: ['shield', 'lock', 'security'],
  锁: ['lock', 'closed'],
  解锁: ['unlock', 'open'],
  可见: ['eye', 'view'],
  隐藏: ['eye-slash', 'eye-off', 'hidden'],
  眼睛: ['eye', 'view'],
  帮助: ['question', 'help', 'info'],
  提示: ['information', 'info', 'question'],
  信息: ['info', 'information'],
  警告: ['warning', 'exclamation', 'alert'],
  错误: ['error', 'x-circle', 'exclamation'],
  成功: ['check', 'success', 'check-circle'],
  失败: ['x', 'error', 'close'],
  星标: ['star', 'favorite'],
  收藏: ['star', 'heart', 'bookmark'],
  喜欢: ['heart', 'like'],
  心: ['heart'],
  标签: ['tag', 'label'],
  分类: ['folder', 'tag', 'squares'],
  品牌: ['tag', 'badge', 'sparkles'],
  属性: ['tag', 'adjust', 'list'],
  规格: ['adjust', 'sliders', 'list'],
  颜色: ['swatch', 'color', 'paint'],
  尺寸: ['scale', 'arrows', 'resize'],
  重量: ['scale', 'weight'],
  温度: ['fire', 'sun', 'thermometer'],
  天气: ['sun', 'cloud', 'weather'],
  太阳: ['sun'],
  月亮: ['moon'],
  云: ['cloud'],
  雨: ['rain', 'cloud'],
  雪: ['snow'],
  火: ['fire'],
  水: ['water', 'drop'],
  电: ['bolt', 'lightning', 'power'],
  工具: ['wrench', 'tool', 'cog'],
  维修: ['wrench', 'tool'],
  构建: ['wrench', 'hammer', 'tool'],
  代码: ['code', 'terminal', 'bracket'],
  开发: ['code', 'terminal', 'cpu'],
  接口: ['api', 'plug', 'link'],
  网络: ['wifi', 'globe', 'signal'],
  云存储: ['cloud', 'server'],
  服务器: ['server', 'cloud', 'computer'],
  监控: ['chart', 'eye', 'presentation'],
  日志: ['document', 'clipboard', 'list'],
  任务: ['clipboard', 'check', 'queue'],
  待办: ['clipboard', 'check'],
  完成: ['check', 'check-circle'],
  进行中: ['clock', 'arrow-path'],
  暂停: ['pause'],
  播放: ['play'],
  停止: ['stop'],
  前进: ['forward', 'arrow-right'],
  后退: ['back', 'arrow-left'],
  上: ['arrow-up', 'chevron-up'],
  下: ['arrow-down', 'chevron-down'],
  左: ['arrow-left', 'chevron-left'],
  右: ['arrow-right', 'chevron-right'],
  展开: ['expand', 'chevron-down', 'plus'],
  收起: ['collapse', 'chevron-up', 'minus'],
  更多: ['ellipsis', 'dots', 'menu'],
  全屏: ['fullscreen', 'arrows-pointing-out'],
  缩小: ['minimize', 'minus', 'compress'],
  放大: ['maximize', 'plus', 'expand'],
  主题: ['sun', 'moon', 'paint'],
  暗黑: ['moon', 'dark'],
  明亮: ['sun', 'light'],
  语言: ['language', 'globe', 'translate'],
  翻译: ['translate', 'language'],
  商城: ['shopping', 'store', 'building-storefront'],
  店铺: ['store', 'building-storefront', 'shop'],
  营销: ['megaphone', 'sparkles', 'gift'],
  活动: ['gift', 'sparkles', 'ticket'],
  客服: ['headset', 'support', 'chat'],
  售后: ['support', 'wrench', 'chat'],
  发票: ['receipt', 'document'],
  合同: ['document', 'clipboard'],
  审批: ['clipboard-check', 'check', 'stamp'],
  流程: ['arrow-path', 'queue', 'list'],
  工单: ['ticket', 'clipboard'],
  供应商: ['truck', 'building', 'user-group'],
  采购: ['cart', 'clipboard', 'truck'],
  入库: ['arrow-down', 'archive', 'inbox'],
  出库: ['arrow-up', 'archive', 'inbox'],
  盘点: ['clipboard-list', 'list', 'check'],
  条码: ['barcode', 'qr-code'],
  包装: ['box', 'cube', 'archive-box'],
  物料: ['cube', 'box', 'archive'],
  生产: ['cog', 'wrench', 'building'],
  质检: ['check-badge', 'shield-check', 'check'],
  发货: ['truck', 'paper-airplane', 'send']
}

const CJK_RE = /[\u4e00-\u9fff\u3400-\u4dbf]/

/** 是否包含中日韩字符 */
export function containsCjk(text: string): boolean {
  return CJK_RE.test(text)
}

/**
 * 将用户输入展开为 Iconify 检索词（中文查表 + 英文原样）
 */
export function expandIconSearchKeywords(keyword: string): string[] {
  const trimmed = keyword.trim()
  if (!trimmed) return []

  const terms = new Set<string>()

  if (containsCjk(trimmed)) {
    const dictHits = ICON_SEARCH_ZH_MAP[trimmed]
    if (dictHits) {
      dictHits.forEach((t) => terms.add(t.toLowerCase()))
    }
    for (const [zh, enList] of Object.entries(ICON_SEARCH_ZH_MAP)) {
      if (trimmed.includes(zh)) {
        enList.forEach((t) => terms.add(t.toLowerCase()))
      }
    }
  }

  trimmed
    .split(/[\s,，、/]+/)
    .map((s) => s.trim().toLowerCase())
    .filter((s) => s && !containsCjk(s))
    .forEach((s) => terms.add(s))

  if (!containsCjk(trimmed)) {
    terms.add(trimmed.toLowerCase())
  }

  return [...terms]
}

/** 图标全名是否命中任一检索词 */
export function iconMatchesSearchTerms(icon: string, terms: string[]): boolean {
  if (!terms.length) return false
  const lower = icon.toLowerCase()
  return terms.some((t) => t.length > 0 && lower.includes(t))
}

/** 供 Iconify 在线搜索 API 使用的 query（空格连接多词） */
export function toIconifySearchQuery(terms: string[]): string {
  return terms.filter(Boolean).join(' ').trim()
}
