# 文件中心设计与实现清单

> Phase 1 MVP：10 类 Category + 5 类 MediaType + 双层文件夹 + 逻辑删除（保留 OSS 物理文件）

## 1. 架构概览

```
前端 file/index
  → Gateway /api/v1/file/**
  → starpivot-file (menu-permission)
       → SysFileController / SysFileFolderController
       → FileStorageService (starpivot-common)
       → OSS / Local
  → MySQL sys_file / sys_file_folder
```

**与 legacy 上传隔离**：`avatar/`、`goods/`、`editor/`、`brand/` 继续走现有接口；文件中心统一 `file/{category}/...`。

---

## 2. Category 枚举

| 编码 | 名称 | OSS 前缀 | folder_id（默认） |
|------|------|----------|------------------|
| SYSTEM | 系统通用 | `file/system/` | 1 |
| OA | 办公审批 | `file/oa/` | 2 |
| CONTRACT | 合同档案 | `file/contract/` | 3 |
| CERT | 资质证件 | `file/cert/` | 4 |
| PROJECT | 项目资料 | `file/project/` | 5 |
| CUSTOMER | 客户资料 | `file/customer/` | 6 |
| GOODS | 商品素材 | `file/goods/` | 7 |
| FINANCE | 财务单据 | `file/finance/` | 8 |
| HR | 人事档案 | `file/hr/` | 9 |
| OTHER | 其他附件 | `file/other/` | 10 |

**对象路径规则**：

```
file/{category小写}/{folderId}/{yyyy/MM/dd}/{uuid}{suffix}
```

示例：`file/oa/1003/2026/06/22/a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf`

---

## 2.1 MediaType 媒体类型（与 Category 正交）

**Category** 表示业务归属（OA、合同等）；**MediaType** 表示文件形态，用于列表筛选、图标展示、预览方式、大小限制。

| 编码 | 名称 | 典型格式 | 默认单文件上限 |
|------|------|----------|---------------|
| `IMAGE` | 图片 | jpg、png、gif、webp、bmp、svg | 10 MB |
| `VIDEO` | 视频 | mp4、webm、mov、avi | 200 MB |
| `DOCUMENT` | 文档 | pdf、doc/docx、xls/xlsx、ppt/pptx、txt、csv | 50 MB |
| `AUDIO` | 音频 | mp3、wav、ogg、m4a、flac | 50 MB |
| `OTHER` | 其他 | zip、rar、7z 及未识别类型 | 50 MB |

**识别规则**（上传时自动写入 `media_type`，无需前端手选）：

1. 优先按 `Content-Type`（MIME）匹配
2. MIME 缺失或不准确时，按扩展名兜底
3. 均未命中 → `OTHER`

**MIME / 扩展名映射（后端 `FileMediaTypeResolver`）**：

| MediaType | MIME 前缀 / 类型 | 扩展名 |
|-----------|-----------------|--------|
| IMAGE | `image/*` | jpg, jpeg, png, gif, webp, bmp, svg |
| VIDEO | `video/*` | mp4, webm, mov, avi, mkv |
| AUDIO | `audio/*` | mp3, wav, ogg, m4a, flac, aac |
| DOCUMENT | `application/pdf`；Office OpenXML；`text/plain`、`text/csv` | pdf, doc, docx, xls, xlsx, ppt, pptx, txt, csv |
| OTHER | 压缩包等 | zip, rar, 7z, tar, gz |

**预览 / 播放策略**：

| MediaType | 前端行为 |
|-----------|---------|
| IMAGE | 弹窗图片预览 |
| VIDEO | 弹窗 `<video>` 播放 |
| AUDIO | 弹窗 `<audio>` 播放 |
| DOCUMENT | PDF 内嵌预览；其余提示下载 |
| OTHER | 仅下载 |

> 注意：业务 Category 中的 `OTHER`（其他附件）与 MediaType 的 `OTHER`（未识别格式）含义不同，字段名已区分（`category` vs `media_type`）。

---

## 3. 数据库

DDL 与菜单初始化见：`sql/init_file_center.sql`

### 逻辑删除约定

| 字段 | 值 | 含义 |
|------|-----|------|
| `del_flag` | `0` | 正常 |
| `del_flag` | `2` | 已删除（回收站） |

删除时额外写入 `delete_by`、`delete_time`；**不删除 OSS 对象**。

MyBatis-Plus 实体使用 `@TableLogic` 映射 `del_flag`，与 `sys_user` 等项目惯例一致。

---

## 4. 后端实现清单

### 4.1 starpivot-api（共享常量）

| 文件 | 说明 |
|------|------|
| `starpivot-api/.../file/FileCategory.java` | 枚举：10 类 + `ossPrefix()` + `defaultFolderId()` |
| `starpivot-api/.../file/FileMediaType.java` | 枚举：IMAGE / VIDEO / DOCUMENT / AUDIO / OTHER |
| `starpivot-api/.../file/FileBizConstants.java` | 默认文件夹名 `DEFAULT_FOLDER_NAME = "默认"` |

```java
public enum FileCategory {
    SYSTEM("SYSTEM", "系统通用", "file/system/", 1L),
    OA("OA", "办公审批", "file/oa/", 2L),
    // ...
    ;
    public static FileCategory of(String code) { ... }
    public String objectPathSegment() { return ossPrefix; } // file/system/
}
```

### 4.2 starpivot-common（存储层扩展）

| 文件 | 改动 |
|------|------|
| `storage/StoragePathValidator.java` | `ALLOWED_PRESIGNED_PREFIXES` 增加 `"file/"` |
| `storage/FileStorageService.java` | 新增 `default void deleteObject(String objectName)`（可选 Phase 2 物理清理） |
| `storage/impl/OssFileStorageService.java` | 实现 `deleteObject` |
| `storage/FileCenterUploadHelper.java`（新建） | 封装 category + folderId → objectName 生成 |
| `storage/FileMediaTypeResolver.java`（新建） | 由 MIME + 扩展名解析 `FileMediaType` |

**上传核心逻辑**（建议在 Helper 中）：

```java
String objectName = String.format("%s%d/%s/%s%s",
    category.getObjectPathSegment(),   // file/oa/
    folderId,
    LocalDate.now().format(DATE_FMT),
    UUID.randomUUID(),
    suffix);
fileStorageService.uploadFileInternal(file, objectName);
```

### 4.3 starpivot-file（业务层）

#### Entity

| 文件 | 表 |
|------|-----|
| `domain/entity/SysFile.java` | sys_file |
| `domain/entity/SysFileFolder.java` | sys_file_folder |

`SysFile` 额外字段：`deleteBy`、`deleteTime`（不用 `@TableLogic` 自动过滤回收站查询，回收站单独 SQL）。

#### DTO / VO

| 文件 | 用途 |
|------|------|
| `domain/dto/SysFileQueryDTO.java` | 分页：folderId、category、**mediaType**、fileName、createBy、时间范围 |
| `domain/dto/SysFileRecycleQueryDTO.java` | 回收站分页 |
| `domain/dto/SysFileUploadDTO.java` | bizType、bizId、remark（multipart 另传 file、folderId） |
| `domain/dto/SysFileFolderDTO.java` | 新建/编辑文件夹 |
| `domain/bo/SysFileVO.java` | 列表/详情 + **mediaType** + displayUrl + previewMode |
| `domain/bo/SysFileFolderVO.java` | 文件夹树节点 |
| `domain/bo/FileCategoryNodeVO.java` | 左侧树：Category + children folders |

#### Mapper

| 文件 | 说明 |
|------|------|
| `mapper/SysFileMapper.java` | 继承 `BaseMapper<SysFile>` |
| `mapper/SysFileFolderMapper.java` | 继承 `BaseMapper<SysFileFolder>` |
| `resources/mapper/SysFileMapper.xml` | 回收站列表、按 folder 统计文件数 |

#### Service

| 接口 | 核心方法 |
|------|---------|
| `ISysFileFolderService` | `listByCategory`、`create`、`update`、`delete`（非空拒绝） |
| `ISysFileService` | `upload`、`pageList`、`getDetail`、`logicDelete`、`restore`、`recyclePage`、`previewUrl` |

**业务规则**：

- 上传：校验 folder 存在；解析 `media_type` 并校验单文件大小（按 MediaType 上限）；**不限制 Category 只能传图片**
- 删除：`del_flag=2`，写 delete 审计字段，不调 OSS delete
- 恢复：`del_flag=0`，清空 delete 字段
- 删文件夹：若文件夹下存在 `del_flag=0` 的文件则拒绝；「默认」文件夹不可删
- 预览：`del_flag=0` 才返回 presigned URL

#### Controller

| 类 | 路径前缀 |
|----|---------|
| `SysFileFolderController` | `/file/folder` |
| `SysFileController` | `/file` |

#### API 明细

**文件夹**

| 方法 | 路径 | 权限 | 请求 | 响应 |
|------|------|------|------|------|
| GET | `/file/folder/tree` | `file:folder:query` | `?category=OA`（可选，空则返回全部 Category 树） | `List<FileCategoryNodeVO>` |
| POST | `/file/folder` | `file:folder:add` | `{ category, folderName, orderNum?, remark? }` | folderId |
| PUT | `/file/folder` | `file:folder:edit` | `{ folderId, folderName?, orderNum?, status? }` | - |
| DELETE | `/file/folder/{folderId}` | `file:folder:delete` | - | - |

**文件**

| 方法 | 路径 | 权限 | 请求 | 响应 |
|------|------|------|------|------|
| POST | `/file/filePageList` | `file:resource:query` | `SysFileQueryDTO` | `PageResponse<SysFileVO>` |
| POST | `/file/upload` | `file:resource:add` | `multipart: file, folderId, bizType?, bizId?, remark?` | `SysFileVO` |
| GET | `/file/{fileId}` | `file:resource:query` | - | `SysFileVO` |
| GET | `/file/preview-url/{fileId}` | `file:resource:query` | - | `{ url, objectName }` |
| DELETE | `/file/removeFile` | `file:resource:delete` | `{ ids: number[] }` | - |
| PUT | `/file/restore` | `file:resource:restore` | `{ ids: number[] }` | - |
| POST | `/file/recycleFilePageList` | `file:resource:query` | `SysFileRecycleQueryDTO` | `PageResponse<SysFileVO>` |

### 4.4 配置改动

**`starpivot-file/src/main/resources/application.yml`**

```yaml
starpivot:
  security:
    authority-strategy: menu-permission
```

**`starpivot-gateway/.../application.yml`** — 新增路由：

```yaml
- id: starpivot-file-manage
  uri: lb://starpivot-file
  predicates:
    - Path=/api/${starpivot.api.version}/file/**
```

**`nacos/config/starpivot-file.yaml`** — 按 MediaType 限制大小（Category 不再绑死 MIME 白名单）：

```yaml
file-center:
  upload:
    max-size-by-media-type:
      IMAGE: 10485760        # 10MB
      VIDEO: 209715200       # 200MB
      DOCUMENT: 52428800     # 50MB
      AUDIO: 52428800        # 50MB
      OTHER: 52428800        # 50MB
```

### 4.5 依赖

`starpivot-file/pom.xml` 增加（若尚未引入）：

```xml
<dependency>
    <groupId>cn.org.starpivot</groupId>
    <artifactId>starpivot-api</artifactId>
</dependency>
```

`starpivot-file` 需能扫描 `AuthMenuMapper`（与 monitor 相同，依赖 common + 同源数据库）。

---

## 5. 前端实现清单

### 5.1 API 层

| 文件 | 说明 |
|------|------|
| `star-pivot-ui/src/api/file/file.ts` | 文件 CRUD、上传、预览、回收站 |
| `star-pivot-ui/src/api/file/folder.ts` | 文件夹树 CRUD |
| `star-pivot-ui/src/api/file/types.ts` | `SysFile`、`SysFileFolder`、`FileCategoryNode` 类型 |

**URL 前缀**（经 Gateway）：`/api/file/...`

示例：

```typescript
export function fetchFileList(params: SysFileQueryParams) {
  return request.post({ url: '/api/file/filePageList', data: params })
}

export function uploadFile(formData: FormData) {
  return request.post({ url: '/api/file/upload', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}
```

### 5.2 页面与组件

| 文件 | 说明 |
|------|------|
| `views/file/index.vue` | 主页面：左树 + 右表 + Tab（全部/回收站） |
| `views/file/modules/file-search.vue` | 搜索：文件名、上传人、时间 |
| `views/file/modules/file-folder-tree.vue` | Category → Folder 双层树 |
| `views/file/modules/file-upload-dialog.vue` | 上传弹窗（选择 folder、拖拽上传） |
| `views/file/modules/file-preview-dialog.vue` | 图片预览 / 非图片下载提示 |
| `views/file/modules/folder-dialog.vue` | 新建/重命名文件夹 |
| `views/file/constants.ts` | Category / **MediaType** 标签、图标、预览组件映射 |

### 5.3 工具扩展

| 文件 | 改动 |
|------|------|
| `utils/storage/oss-object-path.ts` | `OSS_OBJECT_PREFIXES` 增加 `'file/'` |
| `utils/file/file-center.ts`（新建） | `formatFileSize`、`getPreviewMode(mediaType)`、`resolveFileDisplayUrl` |

### 5.4 页面交互

```
┌─────────────────────────────────────────────────────────┐
│ [全部文件] [回收站]     搜索栏                           │
├──────────────┬──────────────────────────────────────────┤
│ Category 树   │  类型筛选：[全部][图片][视频][文档][音频][其他] │
│ ▼ 办公审批    │  工具栏：[上传] [批量删除] [刷新]          │
│   ├ 默认      │  ArtTable：名称/媒体类型/大小/上传人/时间/操作 │
│   └ 2026报销  │  操作：预览/播放 | 下载 | 删除 | 恢复       │
└──────────────┴──────────────────────────────────────────┘
```

**权限指令**：

- `v-auth="'file:resource:add'"` — 上传
- `v-auth="'file:resource:delete'"` — 删除
- `v-auth="'file:folder:add'"` — 新建文件夹

菜单由后端动态路由下发，组件路径 `/file/index` 对应 `views/file/index.vue`。

---

## 6. 业务模块接入指南

业务表推荐存 **`file_id`**（主键引用），可选冗余 `object_name`。

| 场景 | Category | biz_type | 说明 |
|------|----------|----------|------|
| 公告附件 | SYSTEM | `notice` | notice_id 写入 biz_id |
| 报销附件 | OA | `expense` | - |
| 合同扫描 | CONTRACT | `contract` | - |
| 商品手册 | GOODS | `spu` | - |

上传后业务保存 `file_id`；展示时调 `/file/preview-url/{fileId}`。

---

## 7. Phase 2 预留（本期不做）

| 项 | 说明 |
|----|------|
| 物理清理任务 | `del_flag=2` 且 `delete_time < now()-90d` → 删 OSS + 删 DB |
| 引用计数 | `sys_file_ref(file_id, biz_type, biz_id)` |
| 去重 | 上传时写 `file_hash`，相同 hash 可秒传 |
| Category 数据权限 | HR 模块仅 HR 角色可见 |
| 历史 OSS 回填 | 扫描 `goods/` 等写入 sys_file |

---

## 8. 开发顺序建议

1. 执行 `sql/init_file_center.sql`
2. common：`StoragePathValidator` + 上传路径 Helper
3. starpivot-api：`FileCategory` + `FileMediaType` 枚举；common：`FileMediaTypeResolver`
4. starpivot-file：Entity → Mapper → Service → Controller
5. gateway 路由 + file 服务 `menu-permission`
6. 前端 API + `views/file/index.vue`
7. 联调：上传 → 列表 → 预览 → 逻辑删 → 回收站恢复

**预估**：后端 3~4 天，前端 2~3 天，联调 1 天。
