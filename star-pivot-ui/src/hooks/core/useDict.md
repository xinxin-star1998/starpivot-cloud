# useDict Hook 使用文档

## 概述

`useDict` 是一个用于管理字典数据的 Vue 3 Composition API Hook，基于 Pinia Store 实现，提供统一的字典数据获取、缓存、持久化和格式化显示功能。

### 核心特性

- **全局缓存**：字典数据自动缓存，避免重复请求
- **持久化存储**：使用 localStorage 持久化，页面刷新后自动恢复
- **批量加载**：支持同时加载多个字典类型
- **格式化显示**：提供标签显示和下拉选项格式化方法
- **类型安全**：完整的 TypeScript 类型支持
- **灵活配置**：支持强制刷新、自定义默认值等
- **加载状态管理**：防止重复请求，支持加载状态查询

## 安装与导入

```typescript
// 在组件中导入
import { useDict } from '@/hooks/core/useDict'

// 使用
const { loadDict, getDictLabel, getDictOptions } = useDict()
```

## API 方法

### 1. loadDict

加载单个字典类型数据

```typescript
const loadDict = async (dictType: string, force?: boolean): Promise<boolean>
```

**参数说明**：

| 参数       | 类型      | 说明                                   |
| ---------- | --------- | -------------------------------------- |
| `dictType` | `string`  | 字典类型标识（如 `sys_notice_type`）   |
| `force`    | `boolean` | 是否强制刷新，跳过缓存（默认 `false`） |

**返回值**：`boolean` - 是否加载成功

**特性**：

- 如果字典已缓存且 `force=false`，直接返回 `true`
- 支持持久化，页面刷新后自动从 localStorage 恢复
- 防止重复请求，同一字典类型同时只能有一个加载请求

### 2. loadDicts

批量加载多个字典类型数据

```typescript
const loadDicts = async (dictTypes: string[], force?: boolean): Promise<void>
```

**参数说明**：

| 参数        | 类型       | 说明                         |
| ----------- | ---------- | ---------------------------- |
| `dictTypes` | `string[]` | 字典类型标识数组             |
| `force`     | `boolean`  | 是否强制刷新（默认 `false`） |

### 3. getDictLabel

获取字典值对应的标签文本

```typescript
const getDictLabel = (dictType: string, dictValue: string | number | undefined, defaultValue?: string): string
```

**参数说明**：

| 参数           | 类型                            | 说明               |
| -------------- | ------------------------------- | ------------------ |
| `dictType`     | `string`                        | 字典类型标识       |
| `dictValue`    | `string \| number \| undefined` | 字典值             |
| `defaultValue` | `string`                        | 默认值（默认 `-`） |

**返回值**：`string` - 字典标签文本

### 4. getDictItem

获取完整的字典项对象

```typescript
const getDictItem = (dictType: string, dictValue: string | number | undefined): SysDictData | undefined
```

**参数说明**：

| 参数        | 类型                            | 说明         |
| ----------- | ------------------------------- | ------------ |
| `dictType`  | `string`                        | 字典类型标识 |
| `dictValue` | `string \| number \| undefined` | 字典值       |

**返回值**：`SysDictData \| undefined` - 字典项对象

### 5. getDictOptions

获取下拉选择框选项数组

```typescript
const getDictOptions = (dictType: string, addEmpty?: boolean, emptyLabel?: string): { label: string; value: string }[]
```

**参数说明**：

| 参数         | 类型      | 说明                           |
| ------------ | --------- | ------------------------------ |
| `dictType`   | `string`  | 字典类型标识                   |
| `addEmpty`   | `boolean` | 是否添加空选项（默认 `false`） |
| `emptyLabel` | `string`  | 空选项标签（默认 `请选择`）    |

**返回值**：`{ label: string; value: string }[]` - 选项数组

**特性**：

- 自动按 `dictSort` 字段排序
- 支持添加空选项

### 6. getTagType

将字典的 CSS 类名转换为 Element Plus Tag 组件的类型

```typescript
const getTagType = (cssClass?: string): string
```

**参数说明**：

| 参数       | 类型     | 说明                       |
| ---------- | -------- | -------------------------- |
| `cssClass` | `string` | 字典项的 `cssClass` 字段值 |

**映射关系**：

| cssClass  | Tag 类型  |
| --------- | --------- |
| `primary` | `primary` |
| `success` | `success` |
| `warning` | `warning` |
| `danger`  | `danger`  |
| `info`    | `info`    |
| 其他      | `default` |

### 7. isDictLoaded

检查字典类型是否已加载

```typescript
const isDictLoaded = (dictType: string): boolean
```

### 8. isLoading

检查字典类型是否正在加载

```typescript
const isLoading = (dictType: string): boolean
```

### 9. clearDictCache

清除字典缓存

```typescript
const clearDictCache = (dictType?: string): void
```

**参数说明**：

| 参数       | 类型     | 说明                                         |
| ---------- | -------- | -------------------------------------------- |
| `dictType` | `string` | 可选，指定清除的字典类型；不传则清除所有缓存 |

**注意**：清除缓存会同时清除 localStorage 中的持久化数据

### 10. dictCache

获取当前字典缓存（只读）

```typescript
const dictCache: Ref<Map<string, Map<string, SysDictData>>>
```

### 11. loading

获取正在加载的字典类型集合（只读）

```typescript
const loading: Ref<Set<string>>
```

## 使用示例

### 基本用法

```typescript
import { onMounted } from 'vue'
import { useDict } from '@/hooks/core/useDict'

export default {
  setup() {
    const { loadDict, getDictLabel, getDictOptions } = useDict()

    // 组件挂载时加载字典
    onMounted(async () => {
      await loadDict('sys_notice_type')
      await loadDict('sys_notice_status')
    })

    // 获取字典标签
    const typeLabel = getDictLabel('sys_notice_type', '1') // '通知'

    // 获取下拉选项
    const statusOptions = getDictOptions('sys_notice_status', true)
    // [{ label: '请选择', value: '' }, { label: '正常', value: '0' }, { label: '关闭', value: '1' }]

    return {
      typeLabel,
      statusOptions
    }
  }
}
```

### 批量加载字典

```typescript
import { onMounted } from 'vue'
import { useDict } from '@/hooks/core/useDict'

export default {
  setup() {
    const { loadDicts } = useDict()

    onMounted(async () => {
      // 一次性加载多个字典
      await loadDicts(['sys_notice_type', 'sys_notice_status', 'sys_user_status', 'sys_dept_type'])
    })
  }
}
```

### 在表格列中使用（带标签显示）

```typescript
import { h } from 'vue'
import { ElTag } from 'element-plus'
import { useDict } from '@/hooks/core/useDict'

const { getDictItem, getTagType } = useDict()

// 表格列配置
const columns = [
  {
    prop: 'status',
    label: '状态',
    formatter: (row) => {
      const dictItem = getDictItem('sys_notice_status', row.status)
      if (dictItem) {
        return h(ElTag, { type: getTagType(dictItem.cssClass) }, () => dictItem.dictLabel)
      }
      return row.status || '-'
    }
  }
]
```

### 在表单中使用（下拉选择）

```typescript
import { ref, onMounted, computed } from 'vue'
import { useDict } from '@/hooks/core/useDict'

export default {
  setup() {
    const { loadDict, getDictOptions } = useDict()
    const form = ref({
      noticeType: ''
    })

    onMounted(async () => {
      await loadDict('sys_notice_type')
    })

    // 计算属性获取下拉选项
    const noticeTypeOptions = computed(() => {
      return getDictOptions('sys_notice_type', true)
    })

    return {
      form,
      noticeTypeOptions
    }
  }
}
```

```vue
<template>
  <el-select v-model="form.noticeType" placeholder="请选择公告类型">
    <el-option
      v-for="option in noticeTypeOptions"
      :key="option.value"
      :label="option.label"
      :value="option.value"
    />
  </el-select>
</template>
```

### 强制刷新字典

```typescript
const { loadDict } = useDict()

// 强制重新加载，不使用缓存
await loadDict('sys_notice_type', true)
```

### 检查加载状态

```typescript
const { isLoading, isDictLoaded } = useDict()

// 检查是否正在加载
if (isLoading('sys_notice_type')) {
  console.log('字典正在加载中...')
}

// 检查是否已加载
if (isDictLoaded('sys_notice_type')) {
  console.log('字典已加载')
}
```

## 持久化机制

### 工作原理

1. **自动持久化**：字典数据加载后自动保存到 localStorage
2. **页面刷新恢复**：页面刷新后自动从 localStorage 恢复缓存
3. **版本化存储**：使用项目版本化的存储键 `sys-v{version}-dict`
4. **跨组件共享**：所有组件共享同一个字典缓存

### 存储键格式

```
sys-v{version}-dict
```

例如：`sys-v1.0.0-dict`

### 数据结构

```typescript
{
  dictCache: {
    "sys_notice_type": {
      "1": { dictValue: "1", dictLabel: "通知", cssClass: "warning", ... },
      "2": { dictValue: "2", dictLabel: "公告", cssClass: "success", ... }
    },
    "sys_notice_status": {
      "0": { dictValue: "0", dictLabel: "正常", cssClass: "primary", ... },
      "1": { dictValue: "1", dictLabel: "关闭", cssClass: "danger", ... }
    }
  },
  loading: []
}
```

### 清除持久化数据

```typescript
// 清除特定字典
clearDictCache('sys_notice_type')

// 清除所有字典
clearDictCache()
```

## 最佳实践

### 1. 组件挂载时预加载

在组件 `onMounted` 钩子中预加载所需字典，确保数据就绪：

```typescript
onMounted(async () => {
  await loadDicts(['sys_notice_type', 'sys_notice_status'])
})
```

### 2. 全局初始化（可选）

对于全局常用字典，可以在应用启动时统一加载：

```typescript
// src/main.ts
import { useDictStore } from '@/store/modules/dict'

async function initApp() {
  const dictStore = useDictStore()
  await dictStore.loadDicts(['sys_notice_type', 'sys_notice_status', 'sys_user_status'])

  createApp(App).mount('#app')
}

initApp()
```

### 3. 错误处理

建议在加载字典时添加错误处理：

```typescript
try {
  const success = await loadDict('sys_notice_type')
  if (!success) {
    console.error('字典加载失败')
  }
} catch (error) {
  console.error('字典加载异常:', error)
}
```

### 4. 缓存管理

- 字典数据默认全局缓存并持久化
- 使用 `isDictLoaded` 检查字典是否已加载
- 使用 `clearDictCache` 清除特定字典或全部缓存
- 页面刷新后自动从 localStorage 恢复缓存

### 5. 性能优化

- 避免在渲染函数中频繁调用 `loadDict`
- 使用 `isLoading` 防止重复请求
- 合理使用 `force` 参数控制缓存策略

## 注意事项

1. **字典类型必须先加载再使用**：调用 `getDictLabel`、`getDictOptions` 等方法前，必须确保对应的字典类型已通过 `loadDict` 或 `loadDicts` 加载
2. **字典值类型转换**：`getDictLabel` 和 `getDictItem` 会自动将数字类型的字典值转换为字符串进行匹配
3. **持久化存储**：字典数据会自动持久化到 localStorage，页面刷新后自动恢复
4. **全局状态**：字典缓存在所有组件间共享，修改会影响全局
5. **加载状态**：同一字典类型同时只能有一个加载请求，防止重复请求
6. **存储容量**：localStorage 有容量限制（通常 5-10MB），大量字典数据需注意存储空间

## 完整示例

```typescript
import { ref, onMounted, computed } from 'vue'
import { h } from 'vue'
import { ElTag, ElSelect, ElOption } from 'element-plus'
import { useDict } from '@/hooks/core/useDict'

export default {
  setup() {
    const { loadDicts, getDictLabel, getDictItem, getDictOptions, getTagType, isLoading } =
      useDict()

    const form = ref({
      noticeType: '1',
      status: '0'
    })

    const tableData = ref([
      { id: 1, noticeType: '1', status: '0' },
      { id: 2, noticeType: '2', status: '1' }
    ])

    // 加载字典
    onMounted(async () => {
      await loadDicts(['sys_notice_type', 'sys_notice_status'])
    })

    // 获取下拉选项
    const typeOptions = computed(() => getDictOptions('sys_notice_type', true))
    const statusOptions = computed(() => getDictOptions('sys_notice_status', true))

    // 表格列配置
    const columns = [
      { prop: 'id', label: 'ID' },
      {
        prop: 'noticeType',
        label: '公告类型',
        formatter: (row) => {
          const item = getDictItem('sys_notice_type', row.noticeType)
          return item ? h(ElTag, { type: getTagType(item.cssClass) }, () => item.dictLabel) : '-'
        }
      },
      {
        prop: 'status',
        label: '状态',
        formatter: (row) => getDictLabel('sys_notice_status', row.status)
      }
    ]

    return {
      form,
      tableData,
      typeOptions,
      statusOptions,
      columns,
      isLoading
    }
  }
}
```

## 迁移指南

### 从旧版本迁移

如果你之前使用的是模块级别的 `useDict` hook，迁移到 Pinia 版本非常简单：

**旧版本**：

```typescript
import { useDict } from '@/hooks/core/useDict'
const { loadDict, getDictLabel } = useDict()
```

**新版本**：

```typescript
import { useDict } from '@/hooks/core/useDict'
const { loadDict, getDictLabel } = useDict()
```

API 完全兼容，无需修改代码！

### 新增功能

新版本新增了以下功能：

- `isLoading` - 检查字典是否正在加载
- `loading` - 获取正在加载的字典类型集合
- 自动持久化存储
- 页面刷新后自动恢复缓存
