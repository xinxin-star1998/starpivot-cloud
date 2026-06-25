# 支付宝沙箱联调指南

本文说明如何在本地开发环境接入 **支付宝沙箱「电脑网站支付」**，并与 StarPivot 商城 C 端联调。

---

## 1. 前置条件

| 服务 | 默认端口 | 说明 |
|------|----------|------|
| `starpivot-gateway` | **8080** | 对外统一入口，notify 须指向网关 |
| `starpivot-mall` | 9205 | 商城微服务（内网，不直接暴露给支付宝） |
| `star-pivot-ui` | **5173** | C 端前端，`return-url` 指向此地址 |

确保网关、商城、Redis、MySQL 已启动，C 端可正常登录并下单。

---

## 2. 获取沙箱应用与密钥

### 2.1 登录开放平台

1. 打开 [支付宝开放平台](https://open.alipay.com/) 并登录。
2. 进入 **开发者中心 → 沙箱环境**（或 [沙箱应用](https://open.alipay.com/develop/sandbox/app)）。
3. 记录 **沙箱 APPID**（形如 `9021000xxxxxxxx`）。

### 2.2 配置密钥（RSA2）

推荐使用开放平台 **「密钥工具」** 生成 **PKCS8 格式** 应用私钥，并将 **应用公钥** 上传到沙箱应用。

需要保存三份内容：

| 名称 | 用途 | 配置项 |
|------|------|--------|
| 沙箱 APPID | 应用标识 | `MALL_ALIPAY_APP_ID` |
| 应用私钥（PKCS8） | 本系统签名 | `MALL_ALIPAY_PRIVATE_KEY` |
| 支付宝公钥 | 验签回调 | `MALL_ALIPAY_PUBLIC_KEY` |

> **注意**：`MALL_ALIPAY_PUBLIC_KEY` 填的是开放平台沙箱页展示的 **「支付宝公钥」**，不是你自己生成的应用公钥。

私钥可为一整行 Base64（不含 `BEGIN/END` 头尾），也可为多行 PEM，项目会自动识别。

### 2.3 沙箱买家账号

沙箱环境页面会提供 **买家登录账号与支付密码**，在收银台用该账号完成「付款」。

---

## 3. 配置项目

### 3.1 方式 A：本地环境变量（推荐联调）

在启动 `starpivot-mall` 前设置（PowerShell 示例）：

```powershell
$env:MALL_ALIPAY_ENABLED = "true"
$env:MALL_MOCK_PAY_ENABLED = "false"
$env:MALL_ALIPAY_APP_ID = "9021000xxxxxxxx"
$env:MALL_ALIPAY_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC..."
$env:MALL_ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
$env:MALL_ALIPAY_GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do"
# notify 须公网，见第 4 节 ngrok
$env:MALL_ALIPAY_NOTIFY_URL = "https://YOUR-NGROK-ID.ngrok-free.app/api/v1/portal/pay/alipay/notify"
$env:MALL_ALIPAY_RETURN_URL = "http://127.0.0.1:5173/portal/orders"
```

### 3.2 方式 B：Nacos `starpivot-mall.yaml`

在 `nacos/config/starpivot-mall.yaml` 或 Nacos 控制台增加：

```yaml
starpivot:
  mall:
    mock-pay-enabled: false
    alipay:
      enabled: true
      app-id: 9021000xxxxxxxx
      private-key: |
        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...
      alipay-public-key: |
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
      gateway-url: https://openapi-sandbox.dl.alipaydev.com/gateway.do
      notify-url: https://YOUR-NGROK-ID.ngrok-free.app/api/v1/portal/pay/alipay/notify
      return-url: http://127.0.0.1:5173/portal/orders
```

修改 Nacos 后重启 `starpivot-mall`（若已开 refresh，部分项可热更新，密钥类建议重启）。

### 3.3 网关白名单（已内置，一般无需改）

以下路径已在网关与 Mall 安全配置中放行，**无需 JWT**：

```
POST /api/v1/portal/pay/alipay/notify
```

---

## 4. ngrok 暴露本地网关（异步通知必需）

支付宝 **异步通知** 从公网 POST 到你的 `notify-url`，`127.0.0.1` 无法收到，本地必须用隧道。

### 4.1 安装 ngrok

1. 注册 [ngrok](https://ngrok.com/) 并下载 Windows 客户端。
2. 按官网说明配置 authtoken：

```powershell
ngrok config add-authtoken YOUR_NGROK_TOKEN
```

### 4.2 启动隧道

网关监听 **8080**，执行：

```powershell
ngrok http 8080
```

终端会显示类似：

```
Forwarding  https://a1b2c3d4.ngrok-free.app -> http://localhost:8080
```

将 notify 地址设为：

```
https://a1b2c3d4.ngrok-free.app/api/v1/portal/pay/alipay/notify
```

更新 `MALL_ALIPAY_NOTIFY_URL` 或 Nacos 配置后 **重启 starpivot-mall**。

> ngrok 免费域名每次重启会变，需同步更新 notify-url。  
> `return-url` 仍可用 `http://127.0.0.1:5173/portal/orders`（浏览器本地跳回）。

### 4.3 可选：固定域名

ngrok 付费计划或自建 frp / cloudflared 隧道，可得到稳定 notify 地址，避免频繁改配置。

---

## 5. 联调步骤

### 5.1 验证配置是否生效

C 端登录后，浏览器或 curl 请求（需带会员 JWT）：

```
GET http://127.0.0.1:8080/api/v1/portal/pay/alipay/enabled
```

返回 `data: true` 表示支付宝已启用且配置完整。

### 5.2 完整支付流程

1. C 端登录 → 加购 → 结算 → **提交订单**。
2. 弹窗应显示 **「支付宝支付」**（非 Mock）。
3. 点击后新窗口打开支付宝沙箱收银台，用 **沙箱买家账号** 付款。
4. 支付成功后：
   - 浏览器跳转到 `return-url`（订单列表）；
   - 支付宝 POST `notify` → 订单状态变为 **待发货（status=1）**。
5. 在「我的订单」刷新，确认状态已更新。

### 5.3 查看日志

| 现象 | 排查 |
|------|------|
| 按钮仍是 Mock | `enabled` 接口是否为 `false`；检查 APPID/密钥/notify-url |
| 下单报「支付宝下单失败」 | 看 mall 日志；常见为密钥格式错误或 APPID 不匹配 |
| 已付款但订单仍待付款 | notify 未到达；检查 ngrok 是否运行、notify-url 是否正确 |
| notify 验签失败 | 确认用的是 **支付宝公钥**，不是应用公钥 |

Mall 日志关键字：`Alipay notify`、`Alipay page pay failed`。

---

## 6. 接口一览

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/v1/portal/pay/alipay/enabled` | 会员 JWT | 是否启用支付宝 |
| POST | `/api/v1/portal/pay/alipay/order/{orderId}` | 会员 JWT | 返回支付表单 HTML |
| POST | `/api/v1/portal/pay/alipay/notify` | 无 | 支付宝异步通知 |
| PUT | `/api/v1/portal/order/{id}/pay` | 会员 JWT | Mock 支付（`mock-pay-enabled=true` 时） |

---

## 7. 生产环境注意

1. 网关改为 `https` 正式域名，notify/return 均使用 HTTPS。
2. `gateway-url` 改为生产：`https://openapi.alipay.com/gateway.do`。
3. `MALL_MOCK_PAY_ENABLED=false`，仅保留真实支付。
4. 密钥放 Nacos / 密钥管理服务，勿提交到 Git。
5. 支付宝开放平台配置 **电脑网站支付** 产品并审核通过。

---

## 8. 快速检查清单

- [ ] 沙箱 APPID、应用私钥、支付宝公钥已配置
- [ ] `MALL_ALIPAY_ENABLED=true`
- [ ] `MALL_MOCK_PAY_ENABLED=false`（或前端优先走支付宝）
- [ ] ngrok 指向 8080，notify-url 使用 ngrok 的 https 地址
- [ ] 网关、mall 已重启
- [ ] `/portal/pay/alipay/enabled` 返回 `true`
- [ ] 沙箱买家账号可登录收银台
