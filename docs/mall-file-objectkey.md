# 商城文件 objectKey 规范

> 与 `starpivot-file` 文件中心、`starpivot-common` 上传助手对齐。

## 存库字段

业务表（如 `pms_spu_images.img_url`、`pms_sku_images.img_url`）**只存 objectKey**（OSS `objectName`），不存带域名的完整 URL。

| 字段含义 | 示例 |
|---|---|
| objectKey | `file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg` |
| 展示 URL | 由 `MallImageDisplaySupport` / `FileStorageService.getPresignedUrl()` 运行时生成 |

## 命名格式

```
{category.ossPrefix}{folderId}/{yyyy/MM/dd}/{uuid}{ext}
```

| 段 | 说明 | 示例 |
|---|---|---|
| `category.ossPrefix` | `FileCategory` 枚举 | `file/goods/` |
| `folderId` | 文件中心文件夹 ID | `7`（GOODS 默认） |
| 日期 | 上传日 | `2026/06/24` |
| `uuid` | 随机文件名 | `f974367a-...` |
| `ext` | 小写扩展名 | `.jpg` |

生成逻辑：`FileCenterUploadHelper.buildObjectName()`  
校验工具：`cn.org.starpivot.api.file.FileObjectKeyUtils`

## 商城使用的分类

| 业务 | FileCategory | ossPrefix | 上传入口 |
|---|---|---|---|
| 商品图/轮播/专题 | `GOODS` | `file/goods/` | `MallImageUploadSupport`（product / promotion） |
| 品牌 logo 等 | `GOODS` | `file/goods/` | 同上 |

## 兼容旧数据

`StorageObjectPathUtils.normalizeToObjectName()` 可将历史完整 URL 转为 objectKey。  
C 端展示时 `MallImageDisplaySupport` 同时兼容 `http(s)://` 直链与 objectKey。

## 开发约定

1. 新上传一律走 `FileCenterUploadHelper` + `FileCategory.GOODS`
2. 入库前用 `StorageObjectPathUtils.normalizeToObjectName()` 规范化
3. 禁止在业务表写入临时预签名 URL（会过期）
4. 删除商品图时通过 `FileRefClient` 解绑（如有 file_ref 关联）

## 相关代码

- `starpivot-api/.../FileCategory.java`
- `starpivot-api/.../FileObjectKeyUtils.java`
- `starpivot-common/.../FileCenterUploadHelper.java`
- `starpivot-mall-product/.../MallImageUploadSupport.java`
