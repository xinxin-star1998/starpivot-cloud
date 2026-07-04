# StarPivot 微信小程序

基于 **uni-app (Vue3 + TypeScript)**，与 `star-pivot-ui` 管理端、`starpivot-mall` Portal API 共用同一后端。

详细设计见 [docs/miniprogram.md](../docs/miniprogram.md)。

## 快速开始

```bash
# 1. 确保 Gateway(:8080) + Mall(:9205) 已启动

# 2. 安装依赖
npm install

# 3. 编译到微信小程序
npm run dev:mp-weixin
```

用微信开发者工具打开目录：`dist/dev/mp-weixin`。

开发工具中勾选 **不校验合法域名**（本地 `http://localhost:8080`）。

## 环境变量

可在项目根目录创建 `.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_USE_MOCK_LOGIN=true
```

| 变量 | 默认 | 说明 |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | 网关 API 前缀 |
| `VITE_USE_MOCK_LOGIN` | `true` | 使用 `mock_mini_code` 联调登录 |

## 页面（P0）

| 页面 | 路径 |
|---|---|
| 首页 | `pages/index` |
| 商品详情 | `pages/product/detail` |
| 购物车 | `pages/cart` |
| 登录 | `pages/login` |
| 我的 | `pages/account` |
| 确认订单 | `pages/checkout` |
| 收货地址 | `pages/account/addresses` |
| 订单 | `pages/orders` |
| 搜索 | `pages/search` |
| 限时秒杀 | `pages/seckill` |
| 优惠券 | `pages/coupons` |
| 我的收藏 | `pages/account/favorites` |
| 评价中心 | `pages/account/reviews` |
| 编辑资料 | `pages/account/profile` |
| 专题活动 | `pages/subject` |

## 与管理端关系

- 商品、库存、营销：管理端 `star-pivot-ui/views/mall` 维护
- 小程序通过 `/api/v1/portal/**` 只读/下单，不直连 B 端 `/mall/**` API

## 技术栈（与管理端对齐）

| 组件 | 版本 | 说明 |
|---|---|---|
| Vue | 3.5.x | 与 `star-pivot-ui` 统一 |
| TypeScript | 5.6.x | 与 `star-pivot-ui` 统一 |
| Vite | 5.2.8 | uni-app 编译器固定版本，暂无法升至 8.x |
| uni-app | 3.0.0-alpha-5010420260626001 | 最新 Vue3 CLI 编译器 |
