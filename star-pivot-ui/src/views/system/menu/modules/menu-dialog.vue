<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="860px"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :rules="rules"
      :span="width > 640 ? 12 : 24"
      :gutter="20"
      label-width="100px"
      :show-reset="false"
      :show-submit="false"
    >
      <template #menuType>
        <ElRadioGroup v-model="form.menuType" :disabled="disableMenuType">
          <ElRadioButton value="M" :disabled="menuTypeDisabled.M">目录</ElRadioButton>
          <ElRadioButton value="C" :disabled="menuTypeDisabled.C">菜单</ElRadioButton>
          <ElRadioButton value="F" :disabled="menuTypeDisabled.F">按钮</ElRadioButton>
        </ElRadioGroup>
      </template>
      <template #parentId>
        <ElTreeSelect
          v-model="form.parentId"
          :data="parentMenuOptions"
          :props="parentTreeProps"
          :render-after-expand="false"
          check-strictly
          clearable
          placeholder="请选择上级菜单，不选则为顶级菜单"
          style="width: 100%"
        />
      </template>
      <template #icon>
        <ArtIconPicker ref="iconPickerRef" v-model="form.icon" :manual="true">
          <ElInput
            class="menu-icon-input"
            v-model="form.icon"
            placeholder="如：ri:user-line"
            clearable
            style="width: 100%"
          >
            <template #prepend>
              <div class="menu-icon-prepend">
                <Icon :icon="form.icon || 'ri:apps-line'" style="font-size: 18px" />
              </div>
            </template>
            <template #append>
              <ElButton class="menu-icon-append-btn" @click.stop="handleChooseIconClick">
                选择图标
              </ElButton>
            </template>
          </ElInput>
        </ArtIconPicker>
      </template>
    </ArtForm>

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确 定</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {FormRules} from 'element-plus'
import {ElIcon, ElInput, ElMessage, ElTooltip, ElTreeSelect} from 'element-plus'
import {QuestionFilled} from '@element-plus/icons-vue'
import {Icon} from '@iconify/vue'
import {formatMenuTitle} from '@/utils/router'
import {safeError} from '@/utils'
import type {AppRouteRecord} from '@/types/router'
import type {FormItem} from '@/components/core/forms/art-form/index.vue'
import ArtForm from '@/components/core/forms/art-form/index.vue'
import ArtIconPicker from '@/components/core/base/art-icon-picker/index.vue'
import {useWindowSize} from '@vueuse/core'
import type {MenuFormData} from '../types'
import {fetchGetParentMenu, type SysMenu} from '@/api/menu/menu'

const { width } = useWindowSize()

  /**
   * 创建带 tooltip 的表单标签
   * @param label 标签文本
   * @param tooltip 提示文本
   * @returns 渲染函数
   */
  const createLabelTooltip = (label: string, tooltip: string) => {
    return () =>
      h('span', { class: 'flex items-center' }, [
        h('span', label),
        h(
          ElTooltip,
          {
            content: tooltip,
            placement: 'top'
          },
          () => h(ElIcon, { class: 'ml-0.5 cursor-help' }, () => h(QuestionFilled))
        )
      ])
  }

  interface Props {
    visible: boolean
    editData?: AppRouteRecord | any
    rawMenuData?: SysMenu[] // 原始菜单数据，用于回显
    type?: 'menu' | 'button'
    lockType?: boolean
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', data: MenuFormData): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    type: 'menu',
    lockType: false
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const iconPickerRef = ref<{ open: () => void; close: () => void } | null>(null)
  const isEdit = ref(false)
  const parentMenuOptions = ref<SysMenu[]>([])
  const originalMenus = ref<SysMenu[]>([])
  const parentTreeProps = {
    label: 'label',
    value: 'value',
    children: 'children'
  }

  /** 统一 parentId 为 number，避免 ElTreeSelect 因类型不一致只显示 ID */
  const normalizeParentId = (id: unknown): number | undefined => {
    if (id === undefined || id === null || id === '') return undefined
    const num = Number(id)
    return Number.isNaN(num) ? undefined : num
  }

  const form = reactive<MenuFormData>({
    menuType: 'M',
    menuName: '',
    parentId: undefined,
    orderNum: 1,
    path: '',
    component: '',
    query: '',
    routeName: '',
    isFrame: 1,
    isCache: 1,
    visible: '0',
    status: '0',
    perms: '',
    icon: '',
    remark: ''
  })

  /**
   * 根据menuId查找菜单类型
   */
  const findMenuTypeById = (menuId: number, menuList: SysMenu[]): string | undefined => {
    for (const menu of menuList) {
      if (menu.menuId === menuId || menu.value === menuId) {
        return menu.menuType
      }
      if (menu.children && menu.children.length > 0) {
        const found = findMenuTypeById(menuId, menu.children)
        if (found) return found
      }
    }
    return undefined
  }

  /** 路由地址是否为 http(s) 外链（与 MenuProcessor 判定一致） */
  const isHttpExternalPath = (p: string) => p.startsWith('http://') || p.startsWith('https://')

  /** 是否视为顶级菜单（无上级或上级为 0） */
  const isTopLevelMenu = () => !form.parentId || form.parentId === 0
  /** isFrame是否选中 */
  const isFrameChecked = () => form.isFrame === 0

  // 路径验证函数（是否外链由 isFrame 决定：选中则按 http(s) 外链规则，否则按站内路径规则）
  const validatePath = (rule: any, value: string, callback: any) => {
    if (!value && form.menuType === 'C') {
      callback(new Error('菜单类型必须填写路由地址'))
      return
    }
    if (value) {
      // 检查是否包含非法字符
      if (/[<>"']/.test(value)) {
        callback(new Error('路由地址不能包含特殊字符 < > " \''))
        return
      }
      if (isFrameChecked()) {
        if (!isHttpExternalPath(value)) {
          callback(new Error('外链菜单：路由地址请以 http:// 或 https:// 开头的完整地址'))
          return
        }
        callback()
        return
      }
      // 站内：一级须以 / 开头或完整 http(s)（未勾选外链时仍允许按路径识别为外链地址）
      if (isTopLevelMenu() && !value.startsWith('/') && !isHttpExternalPath(value)) {
        callback(
          new Error(
            '一级菜单：站内路由请以 / 开头；若需外链请先勾选「是否外链」或填写 http(s):// 地址'
          )
        )
        return
      }
      if (form.parentId && value.startsWith('/') && !value.startsWith('http')) {
        callback(new Error('二级及以下菜单的路由地址不能以 / 开头'))
        return
      }
    }
    callback()
  }

  // 组件路径验证函数
  const validateComponent = (rule: any, value: string, callback: any) => {
    const pathTrim = (form.path || '').trim()
    const externalByPath = isHttpExternalPath(pathTrim)
    const externalMenu = isFrameChecked() || externalByPath
    // 外链（勾选或地址为 http(s)）无需组件
    if (form.menuType === 'C' && !value && !externalMenu) {
      callback(new Error('菜单类型必须填写组件路径'))
      return
    }
    if (value) {
      // 组件路径必须以 / 开头
      if (!value.startsWith('/')) {
        callback(new Error('组件路径必须以 / 开头'))
        return
      }
      // 检查是否包含非法字符
      if (/[<>"']/.test(value)) {
        callback(new Error('组件路径不能包含特殊字符 < > " \''))
        return
      }
    }
    callback()
  }

  // 权限标识验证函数
  const validatePerms = (rule: any, value: string, callback: any) => {
    if (form.menuType === 'F' && !value) {
      callback(new Error('按钮类型必须填写权限标识'))
      return
    }
    if (value) {
      // 权限标识格式验证：允许字母、数字、冒号、下划线
      if (!/^[a-zA-Z0-9:_-]+$/.test(value)) {
        callback(new Error('权限标识只能包含字母、数字、冒号、下划线和横线'))
        return
      }
    }
    callback()
  }

  const rules = reactive<FormRules>({
    menuName: [
      { required: true, message: '请输入菜单名称', trigger: 'blur' },
      { max: 50, message: '菜单名称长度不能超过50个字符', trigger: 'blur' }
    ],
    menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
    path: [{ validator: validatePath, trigger: 'blur' }],
    component: [{ validator: validateComponent, trigger: 'blur' }],
    perms: [{ validator: validatePerms, trigger: 'blur' }]
  })

  /**
   * 根据菜单类型动态生成表单项
   */
  const formItems = computed<FormItem[]>(() => {
    // 菜单类型和上级菜单放在同一行
    const baseItems: FormItem[] = [
      { label: '菜单类型', key: 'menuType', span: 12 },
      {
        label: '上级菜单',
        key: 'parentId',
        span: 12
      }
    ]

    // 按钮类型（F）只显示基本字段
    if (form.menuType === 'F') {
      return [
        ...baseItems,
        {
          label: '按钮名称',
          key: 'menuName',
          type: 'input',
          props: { placeholder: '请输入按钮名称' }
        },
        {
          label: '权限标识',
          key: 'perms',
          type: 'input',
          props: { placeholder: '如：system:user:add' }
        },
        {
          label: '显示顺序',
          key: 'orderNum',
          type: 'number',
          props: { min: 1, controlsPosition: 'right', style: { width: '100%' } }
        },
        {
          label: '备注',
          key: 'remark',
          type: 'input',
          span: 24,
          props: { type: 'textarea', rows: 3, placeholder: '请输入备注' }
        }
      ]
    }

    // 目录（M）和菜单（C）类型
    const switchSpan = width.value < 640 ? 12 : 6
    // 读取 isFrame 以建立依赖：外链选中时路由地址提示外链填写规则
    const externalFrame = isFrameChecked()

    const pathTooltipInternal =
      '一级菜单：以 / 开头的绝对路径（如 /dashboard）\n二级及以下：相对路径（如 console、user）'
    const pathTooltipExternal =
      '外链菜单：请填写完整地址，必须以 http:// 或 https:// 开头（如 https://www.example.com）'

    return [
      ...baseItems,
      {
        label: '菜单名称',
        key: 'menuName',
        type: 'input',
        props: { placeholder: '请输入菜单名称' }
      },
      {
        label: createLabelTooltip(
          '路由地址',
          externalFrame ? pathTooltipExternal : pathTooltipInternal
        ),
        key: 'path',
        type: 'input',
        props: {
          placeholder: externalFrame
            ? '如：https://www.example.com/path'
            : '如：/dashboard 或 console'
        }
      },
      {
        label: createLabelTooltip(
          '路由名称',
          '路由的唯一标识，用于路由跳转，建议使用大驼峰命名（如：UserManage）'
        ),
        key: 'routeName',
        type: 'input',
        props: { placeholder: '如：UserManage' }
      },
      {
        label: createLabelTooltip(
          '组件路径',
          '一级父级菜单：填写 /index/index\n具体页面：填写组件路径（如 /system/user）\n目录菜单：留空'
        ),
        key: 'component',
        type: 'input',
        props: {
          placeholder:
            '如：一级父级菜单：填写 /index/index\n具体页面：填写组件路径（如 /system/user）\n目录菜单：留空'
        }
      },
      {
        label: '路由参数',
        key: 'query',
        type: 'input',
        props: { placeholder: '如：id=1&type=edit' }
      },
      { label: '菜单图标', key: 'icon', type: 'input', props: { placeholder: '如：ri:user-line' } },
      {
        label: '权限标识',
        key: 'perms',
        type: 'input',
        props: { placeholder: '如：system:user:list' }
      },
      {
        label: '显示顺序',
        key: 'orderNum',
        type: 'number',
        props: { min: 1, controlsPosition: 'right', style: { width: '100%' } }
      },
      {
        label: '是否外链',
        key: 'isFrame',
        type: 'switch',
        span: switchSpan,
        props: {
          activeValue: 0,
          inactiveValue: 1
        }
      },
      {
        label: '是否缓存',
        key: 'isCache',
        type: 'switch',
        span: switchSpan,
        props: {
          activeValue: 0,
          inactiveValue: 1
        }
      },
      {
        label: '是否显示',
        key: 'visible',
        type: 'switch',
        span: switchSpan,
        props: {
          activeValue: '0',
          inactiveValue: '1'
        }
      },
      {
        label: '是否启用',
        key: 'status',
        type: 'switch',
        span: switchSpan,
        props: {
          activeValue: '0',
          inactiveValue: '1'
        }
      },
      {
        label: '备注',
        key: 'remark',
        type: 'input',
        span: 24,
        props: { type: 'textarea', rows: 3, placeholder: '请输入备注0/500' }
      }
    ]
  })

  const dialogTitle = computed(() => {
    // 使用宽泛的 Record 类型，避免以 any 索引窄类型对象导致的 TS 报错
    const menuTypeMap: Record<string, string> = {
      M: '目录',
      C: '菜单',
      F: '按钮'
    }
    const type = menuTypeMap[form.menuType] || '菜单'
    return isEdit.value ? `编辑${type}` : `新建${type}`
  })

  /**
   * 获取当前选择的上级菜单类型
   */
  const getParentMenuType = computed(() => {
    if (form.parentId === 0 || form.parentId === undefined) {
      return undefined // 顶级菜单，无上级
    }
    return findMenuTypeById(form.parentId, originalMenus.value)
  })

  /**
   * 是否禁用菜单类型切换
   */
  const disableMenuType = computed(() => {
    if (isEdit.value) return true
    if (!isEdit.value && form.menuType === 'M' && props.lockType) return true
    return false
  })

  /**
   * 菜单类型选项的禁用状态
   */
  const menuTypeDisabled = computed(() => {
    const parentType = getParentMenuType.value
    if (parentType === 'C') {
      // 如果上级菜单是'C'（菜单），则禁用'M'（目录）选项
      return {
        M: true, // 禁用目录
        C: false, // 允许菜单
        F: false // 允许按钮
      }
    }
    return {
      M: false,
      C: false,
      F: false
    }
  })

  /**
   * 加载上级菜单选项（树形结构）
   * 接口已返回 label 和 value 字段，转换为树形结构
   */
  const loadParentMenuOptions = async (): Promise<void> => {
    try {
      const menus = await fetchGetParentMenu()
      const treeData = Array.isArray(menus) ? menus : []
      // 保存原始菜单数据，用于后续查找菜单类型
      originalMenus.value = treeData
      parentMenuOptions.value =
        treeData.length > 0
          ? treeData
          : [{ menuName: '无上级菜单', label: '无上级菜单', value: 0, children: [] } as SysMenu]
    } catch (error) {
      safeError('加载上级菜单失败:', error)
      parentMenuOptions.value = [
        { menuName: '无上级菜单', label: '无上级菜单', value: 0, children: [] } as SysMenu
      ]
    }
  }

  /**
   * 重置表单数据
   */
  const resetForm = (): void => {
    Object.assign(form, {
      menuType: props.type === 'button' ? 'F' : 'M',
      menuName: '',
      parentId: undefined,
      orderNum: 1,
      path: '',
      component: '',
      query: '',
      routeName: '',
      isFrame: 1,
      isCache: 1,
      visible: '0',
      status: '0',
      perms: '',
      icon: '',
      remark: ''
    })
    // ArtForm 组件暴露的是 reset() 方法，而不是 resetFields()
    // 如果需要清除验证状态，可以通过 ref 访问内部的 formInstance
    nextTick(() => {
      if (formRef.value?.ref) {
        formRef.value.ref.resetFields()
      }
    })
    isEdit.value = false
  }

  /**
   * 从原始菜单数据中查找菜单项
   * @param menuId 菜单ID
   * @param menuList 菜单列表
   * @returns 菜单项
   */
  const findRawMenu = (menuId: number | undefined, menuList: SysMenu[]): SysMenu | undefined => {
    if (!menuId || !menuList) return undefined

    for (const menu of menuList) {
      if (menu.menuId === menuId) {
        return menu
      }
      if (menu.children && menu.children.length > 0) {
        const found = findRawMenu(menuId, menu.children)
        if (found) return found
      }
    }
    return undefined
  }

  /**
   * 加载表单数据（编辑模式）
   * 优先使用接口返回的原始数据
   */
  const loadFormData = (): void => {
    if (!props.editData) return

    const row = props.editData

    // 只有当 row.id 存在时，才认为是编辑模式
    // 如果只有 parentId 而没有 id，说明是新增模式
    if (row.id) {
      isEdit.value = true
    } else {
      isEdit.value = false
    }

    // 从原始数据中查找菜单项
    const rawMenu = row.id && props.rawMenuData ? findRawMenu(row.id, props.rawMenuData) : null

    // 如果是按钮类型
    if (props.type === 'button' || row.meta?.isAuthButton) {
      form.menuType = 'F'
      form.menuId = row.id || undefined
      // 优先使用原始数据
      form.menuName = rawMenu?.menuName || row.meta?.title || row.title || row.menuName || ''
      form.perms = rawMenu?.perms || row.meta?.authMark || row.authMark || row.perms || ''
      form.parentId = normalizeParentId(rawMenu?.parentId ?? row.parentId)
      form.orderNum = rawMenu?.orderNum || row.meta?.orderNum || row.orderNum || 1
      form.remark = rawMenu?.remark || row.remark || ''
      return
    }

    // 目录或菜单类型 - 优先使用原始数据
    form.menuId = row.id || undefined
    form.menuName = rawMenu?.menuName || formatMenuTitle(row.meta?.title || row.menuName || '')
    form.parentId = normalizeParentId(rawMenu?.parentId ?? row.parentId)
    form.orderNum = rawMenu?.orderNum || row.meta?.orderNum || row.orderNum || 1
    form.path = rawMenu?.path || row.path || ''
    form.component = rawMenu?.component || row.component || ''
    form.query = rawMenu?.query || row.query || ''
    form.routeName = rawMenu?.routeName || row.name || row.routeName || ''
    form.isFrame =
      rawMenu?.isFrame !== undefined
        ? rawMenu.isFrame
        : row.meta?.isIframe === true
          ? 0
          : (row.isFrame ?? 1)
    form.isCache =
      rawMenu?.isCache !== undefined
        ? rawMenu.isCache
        : row.meta?.keepAlive === true
          ? 0
          : (row.isCache ?? 1)
    form.visible = rawMenu?.visible || (row.meta?.isHide === true ? '1' : (row.visible ?? '0'))
    form.status = rawMenu?.status || row.status || '0'
    form.perms = rawMenu?.perms || row.meta?.authList?.[0]?.authMark || row.perms || ''
    form.icon = rawMenu?.icon || row.meta?.icon || row.icon || ''
    form.remark = rawMenu?.remark || row.remark || ''

    // 根据原始数据中的 menuType 判断，如果没有则根据是否有component判断
    if (rawMenu?.menuType) {
      form.menuType = rawMenu.menuType as 'M' | 'C' | 'F'
    } else if (!form.component || form.component === '') {
      form.menuType = 'M'
    } else {
      form.menuType = 'C'
    }
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      // 构建提交数据
      const submitData: MenuFormData = {
        menuType: form.menuType,
        menuName: form.menuName,
        parentId: form.parentId === 0 ? undefined : form.parentId,
        orderNum: form.orderNum,
        path: form.path || undefined,
        // 目录类型或组件路径为空时，明确传递空字符串以清空后端数据
        // 如果传递 undefined，后端可能不会更新该字段，导致原来的值保留
        component:
          form.menuType === 'M' || !form.component || form.component.trim() === ''
            ? ''
            : form.component,
        query: form.query || undefined,
        routeName: form.routeName || undefined,
        isFrame: form.isFrame,
        isCache: form.isCache,
        visible: form.visible,
        status: form.status,
        // 如果 perms 为空，明确传递空字符串以清空后端数据
        // 如果传递 undefined，后端可能不会更新该字段，导致原来的值保留
        perms: form.perms && form.perms.trim() !== '' ? form.perms : '',
        icon: form.icon || undefined,
        remark: form.remark || undefined
      }

      // 如果是编辑模式，添加menuId
      if (isEdit.value && form.menuId) {
        submitData.menuId = form.menuId
      }

      emit('submit', submitData)
      handleCancel()
    } catch {
      ElMessage.error('表单校验失败，请检查输入')
    }
  }

  /**
   * 取消操作
   */
  const handleCancel = (): void => {
    emit('update:visible', false)
  }

  /**
   * 对话框关闭后的回调
   */
  const handleClosed = (): void => {
    resetForm()
  }

  /**
   * 监听对话框显示状态
   */
  watch(
    () => props.visible,
    async (newVal: boolean) => {
      if (newVal) {
        // 先加载上级菜单选项，再重置并回显，确保 TreeSelect 能解析 label
        await loadParentMenuOptions()
        resetForm()
        await nextTick()
        if (props.editData) {
          loadFormData()
        }
      }
    }
  )

  /**
   * 监听上级菜单变化，自动调整菜单类型
   */
  watch(
    [() => form.parentId, () => getParentMenuType.value],
    ([newParentId, newParentType]: [number | undefined, string | undefined]) => {
      if (newParentType === 'C' && form.menuType === 'M') {
        // 如果上级菜单是'C'（菜单），且当前选择的是'M'（目录），则自动切换到'C'（菜单）
        if (newParentId !== undefined) {
          form.menuType = 'C'
        }
      }
    }
  )

  /**
   * 监听菜单类型变化，清除校验状态（规则内已读 form.menuType / form.isFrame）
   */
  watch(
    () => form.menuType,
    () => {
      nextTick(() => {
        formRef.value?.ref?.clearValidate()
      })
    },
    { immediate: true }
  )

  /** 是否外链切换时，路由/组件规则不同，立即按新规则重验 */
  watch(
    () => form.isFrame,
    () => {
      nextTick(() => {
        const elForm = formRef.value?.ref
        if (!elForm) return
        elForm.clearValidate(['path', 'component'])
        elForm.validateField(['path', 'component']).catch(() => {})
      })
    }
  )
  onMounted(async () => {
    await loadParentMenuOptions()
  })

  const handleChooseIconClick = () => {
    iconPickerRef.value?.open()
  }
</script>

<style scoped lang="scss">
  .menu-icon-prepend {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 32px;
    color: var(--el-text-color-regular);
    cursor: pointer;
  }

  /* 让输入组更接近截图的“分段”观感 */
  :deep(.menu-icon-input.el-input-group) {
    .el-input-group__prepend {
      padding: 0;
      background: var(--el-fill-color-light);
    }

    .el-input-group__append {
      padding: 0;
      background: var(--el-fill-color-light);
    }
  }

  :deep(.menu-icon-append-btn) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 32px;
    min-width: 112px;
    padding: 0 14px;
    font-size: 13px;
    line-height: 32px;
    white-space: nowrap;
    color: var(--el-color-primary);
    border: 0;
    border-left: 1px solid var(--el-border-color);
    border-radius: 0 4px 4px 0;
    background: var(--default-box-color);

    &:hover {
      background: var(--el-fill-color-light);
    }
  }
</style>
