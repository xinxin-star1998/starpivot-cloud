# StarPivot 微信小程序

> 工程：`star-pivot-mp`（uni-app Vue3 + TypeScript）  
> 后端：复用商城微服务 `starpivot-mall-*` 的 `/api/v1/portal/**`，新增小程序登录与 JSAPI 支付

## 1. 整体架构

```
┌──────────────────┐     ┌──────────────┐     ┌─────────────────────────┐
│  star-pivot-mp   │────▶│   Gateway    │────▶│  starpivot-mall-*       │
│  (微信小程序)     │     │   :8080      │     │  /portal/** + 管理端 API │
└──────────────────┘     └──────────────┘     └─────────────────────────┘
         │                                              │
         │ wx.login / wx.requestPayment                 │ star-pivot-ui
         ▼                                              ▼ (B 端 + H5 Portal)
   微信开放平台 / 商户平台
```

| 端 | 工程 | 说明 |
|---|---|---|
| 管理后台 | `star-pivot-ui` | 商品/订单/营销等 B 端 |
| H5 商城 | `star-pivot-ui/views/portal` | PC/H5 C 端 |
| 微信小程序 | `star-pivot-mp` | 与 Portal 共用同一套后端 |

## 2. API 基址

| 环境 | 小程序 `config/index.ts` | 说明 |
|---|---|---|
| 本地开发 | `http://localhost:8080/api/v1` | 需在微信开发者工具勾选「不校验合法域名」 |
| 生产 | `https://<域名>/api/v1` | 须在小程序后台配置 request 合法域名 |

鉴权：请求头 `Authorization: Bearer <token>`，与 H5 Portal 一致。

## 3. 小程序专属接口（本期新增）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/portal/auth/wechat/mini/login` | `wx.login` 的 code 换 JWT，未注册则自动注册 |
| POST | `/portal/pay/wx/jsapi/{orderId}` | 返回 `wx.requestPayment` 所需签名参数 |

其余接口与 [商城开发事项](./商城开发事项.md) 中 Portal 列表一致。

### 3.1 登录流程

```
小程序 wx.login() → code
    → POST /portal/auth/wechat/mini/login { code }
    → 服务端 jscode2session → unionid/openid
    → 查/建 ums_member + ums_member_auth
    → 返回 { token, member }
    → wx.setStorageSync 持久化
```

### 3.2 支付流程

```
待付款订单 → POST /portal/pay/wx/jsapi/{orderId}
    → 服务端用会员微信 openid 调 JSAPI 预下单
    → 返回 timeStamp / nonceStr / package / signType / paySign
    → wx.requestPayment → 微信异步 notify → 订单状态更新
```

Mock 模式（未配置商户号）：返回 `mock: true`，可再调 `POST /portal/pay/wx/mock/{orderId}` 模拟支付成功。

## 4. 后端配置

`starpivot.mall.portal-auth.mini-program`（Nacos `starpivot-mall-member.yaml`）：

| 配置项 | 环境变量 | 说明 |
|---|---|---|
| `enabled` | `MALL_MINI_PROGRAM_ENABLED` | 是否启用真实 code2session |
| `mock-enabled` | `MALL_MINI_PROGRAM_MOCK` | 开发 Mock（默认 true） |
| `app-id` | `MALL_MINI_PROGRAM_APP_ID` | 小程序 AppId |
| `app-secret` | `MALL_MINI_PROGRAM_APP_SECRET` | 小程序 AppSecret |
| `auto-register` | — | 未绑定微信时自动注册会员 |

微信支付 JSAPI 复用 `starpivot.mall.wxpay`，**`app-id` 须与小程序 AppId 一致**。

## 5. 本地开发

### 5.1 启动后端

按 [README](../README.md) 启动 Gateway + 商城五域微服务（及 MySQL/Redis/Nacos）。

**商品图片：** 须在 Nacos `oss-config.yaml` 中启用 OSS（`oss.enabled=true`，与 system 服务共用桶）。修改配置后需**重启 product 等相关服务**并执行 `.\nacos\import-config.ps1` 同步。

### 5.2 启动小程序

```bash
cd star-pivot-mp
npm install
npm run dev:mp-weixin
```

用微信开发者工具打开 `star-pivot-mp/dist/dev/mp-weixin`。

### 5.3 Mock 联调

默认 `mini-program.mock-enabled=true`：

1. 登录页点「微信一键登录」→ 使用固定 mock code
2. 下单后支付 → JSAPI 返回 mock，再调 mock 确认接口

## 6. 页面规划

| 阶段 | 页面 | Portal API |
|---|---|---|
| P0（本期骨架） | 首页、商品详情、购物车、登录、我的 | home, product, cart, auth |
| P1（已完成） | 结算、地址管理、订单支付/收货 | order, address, region, pay |
| P2（已完成） | 搜索、秒杀、优惠券、收藏、评价 | product, seckill, coupon, collect, comment |
| P3（已完成） | 结算选券、会员中心、专题、分享 | member/center, coupon/checkout, subject |
| P4（已完成） | 头像上传、积分抵扣、订单详情、浏览足迹、账号安全 | image/upload, order/{id}, member/auth |
| P5（已完成） | 申请退货、物流查询、订单角标、规格弹层、相关推荐 | order/return, status-counts, product/{id}/related |

## 7. 与管理端协同

- **商品/库存/订单**：管理端在 `star-pivot-ui/views/mall` 维护，小程序只读 Portal 接口
- **轮播/营销**：后台 SMS/CMS 配置，小程序首页 `GET /portal/home` 拉取
- **会员数据**：小程序与 H5 共用 `ums_member`，同一微信 unionid 可打通账号（需同一开放平台）

## 8. 上线检查清单

- [ ] 小程序 AppId / AppSecret 写入 Nacos
- [ ] 微信支付商户号、证书、notify 公网 URL
- [ ] 网关 HTTPS 域名加入小程序 request 合法域名
- [ ] `mini-program.mock-enabled=false`、`wxpay.mock-enabled=false`
- [ ] 关闭开发者工具「不校验合法域名」做一次真机验证
