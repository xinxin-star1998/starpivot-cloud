<template>
  <ElForm ref="formRef" :model="model" label-width="160px" class="path-config-form">
    <ElRow :gutter="16">
      <ElCol :span="24">
        <ElFormItem label="模块预设">
          <ElSelect
            v-model="preset"
            placeholder="内置预设"
            clearable
            style="width: 180px"
            @change="applyBuiltinPreset"
          >
            <ElOption label="system 模块" value="system" />
            <ElOption label="mall 模块" value="mall" />
            <ElOption label="monitor 模块" value="monitor" />
          </ElSelect>
          <ElSelect
            v-model="savedPresetKey"
            placeholder="我的预设"
            clearable
            style="width: 180px"
            class="ml-2"
            @change="applySavedPreset"
          >
            <ElOption
              v-for="item in savedPresetList"
              :key="item.name"
              :label="item.name"
              :value="item.name"
            />
          </ElSelect>
          <ExternalActionBtn
            what="自动填子路径"
            usage="根据「基础包名」推导 entity/dto/controller 等 Java 包路径及 mapper、api、vue 目录。"
            link
            type="primary"
            class="ml-3"
            @click="syncFromBasePackage"
          >
            根据基础包填充子路径
          </ExternalActionBtn>
        </ElFormItem>
      </ElCol>

      <ElCol :span="24">
        <ElFormItem label="保存为预设">
          <ElInput
            v-model="newPresetName"
            placeholder="预设名称，如 mall-brand"
            style="width: 220px"
            maxlength="32"
          />
          <ExternalActionBtn
            what="存路径预设"
            usage="将当前路径配置保存到浏览器 localStorage，可在「我的预设」中快速切换。"
            type="primary"
            class="ml-2"
            @click="saveCurrentAsPreset"
          >
            保存预设
          </ExternalActionBtn>
          <ExternalActionBtn
            v-if="savedPresetKey"
            what="删路径预设"
            usage="删除当前选中的「我的预设」，不影响已填写的表单内容。"
            link
            type="danger"
            class="ml-2"
            @click="deleteSavedPreset"
          >
            删除当前预设
          </ExternalActionBtn>
        </ElFormItem>
      </ElCol>

      <ElCol :span="12">
        <ElFormItem label="基础包名" required>
          <ElInput
            v-model="model.basePackage"
            placeholder="com.star.pivot.mall"
            @blur="onBasePackageBlur"
          />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="作者">
          <ElInput v-model="author" placeholder="代码注释作者" />
        </ElFormItem>
      </ElCol>

      <ElCol :span="24"><div class="section-title">Java 包路径</div></ElCol>
      <ElCol :span="12">
        <ElFormItem label="实体 Entity">
          <ElInput v-model="model.entityPackage" placeholder="com.star.pivot.mall.domain.entity" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="DTO">
          <ElInput v-model="model.dtoPackage" placeholder="com.star.pivot.mall.domain.dto" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="VO (Bo)">
          <ElInput v-model="model.voPackage" placeholder="com.star.pivot.mall.domain.bo" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="Bo (ReqBo)">
          <ElInput v-model="model.boPackage" placeholder="com.star.pivot.mall.domain.bo" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="Mapper">
          <ElInput v-model="model.mapperPackage" placeholder="com.star.pivot.mall.mapper" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="Service">
          <ElInput v-model="model.servicePackage" placeholder="com.star.pivot.mall.service" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="ServiceImpl">
          <ElInput
            v-model="model.serviceImplPackage"
            placeholder="com.star.pivot.mall.service.impl"
          />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="Controller">
          <ElInput v-model="model.controllerPackage" placeholder="com.star.pivot.mall.controller" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="24">
        <ElFormItem label="Mapper XML 目录">
          <ElInput v-model="model.mapperXmlPath" placeholder="main/resources/mapper/mall" />
          <div class="field-hint"
            >写盘/Diff 中显示为 main/resources/mapper/...，对应 star-pivot-module 下资源目录</div
          >
        </ElFormItem>
      </ElCol>

      <ElCol :span="24"><div class="section-title">写盘相对路径（相对项目根目录）</div></ElCol>
      <ElCol :span="12">
        <ElFormItem label="API 目录">
          <ElInput v-model="model.apiPath" placeholder="star-pivot-ui/src/api/mall" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="页面目录">
          <ElInput v-model="model.vuePagePath" placeholder="star-pivot-ui/src/views/mall/brand" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="24">
        <ElFormItem label="子组件 modules">
          <ElInput v-model="model.vueModulesPath" placeholder="留空则使用 {页面目录}/modules" />
        </ElFormItem>
      </ElCol>
    </ElRow>
  </ElForm>
</template>

<script setup lang="ts">
  import { ElForm, ElFormItem, ElInput, ElMessage, ElOption, ElSelect } from 'element-plus'
  import ExternalActionBtn from './external-action-btn.vue'
  import type { GenPathProfile } from '@/api/generator/gen-external'

  const PRESET_STORAGE_KEY = 'gen_external_path_presets'
  const LAST_PROFILE_KEY = 'gen_external_last_path_profile'

  interface SavedPreset {
    name: string
    profile: GenPathProfile
  }

  const model = defineModel<GenPathProfile>({ required: true })
  const author = defineModel<string>('author', { default: '' })

  const preset = ref<string>()
  const savedPresetKey = ref<string>()
  const newPresetName = ref('')
  const savedPresetList = ref<SavedPreset[]>([])

  const PRESETS: Record<string, Partial<GenPathProfile>> = {
    system: {
      basePackage: 'com.star.pivot.system',
      mapperXmlPath: 'main/resources/mapper/system',
      apiPath: 'star-pivot-ui/src/api/system',
      vuePagePath: 'star-pivot-ui/src/views/system/demo'
    },
    mall: {
      basePackage: 'com.star.pivot.mall',
      mapperXmlPath: 'main/resources/mapper/mall',
      apiPath: 'star-pivot-ui/src/api/mall',
      vuePagePath: 'star-pivot-ui/src/views/mall/demo'
    },
    monitor: {
      basePackage: 'com.star.pivot.monitor',
      mapperXmlPath: 'main/resources/mapper/monitor',
      apiPath: 'star-pivot-ui/src/api/monitor',
      vuePagePath: 'star-pivot-ui/src/views/monitor/demo'
    }
  }

  function loadSavedPresets() {
    try {
      const raw = localStorage.getItem(PRESET_STORAGE_KEY)
      savedPresetList.value = raw ? (JSON.parse(raw) as SavedPreset[]) : []
    } catch {
      savedPresetList.value = []
    }
  }

  function persistSavedPresets() {
    localStorage.setItem(PRESET_STORAGE_KEY, JSON.stringify(savedPresetList.value))
  }

  function moduleFromBase(base: string): string {
    const idx = base.lastIndexOf('.')
    return idx >= 0 ? base.slice(idx + 1) : base
  }

  function syncFromBasePackage() {
    const base = model.value.basePackage?.trim()
    if (!base) return
    const module = moduleFromBase(base)
    model.value.entityPackage = `${base}.domain.entity`
    model.value.dtoPackage = `${base}.domain.dto`
    model.value.voPackage = `${base}.domain.bo`
    model.value.boPackage = `${base}.domain.bo`
    model.value.mapperPackage = `${base}.mapper`
    model.value.servicePackage = `${base}.service`
    model.value.serviceImplPackage = `${base}.service.impl`
    model.value.controllerPackage = `${base}.controller`
    model.value.mapperXmlPath = `main/resources/mapper/${module}`
    model.value.apiPath = `star-pivot-ui/src/api/${module}`
    if (!model.value.vuePagePath) {
      model.value.vuePagePath = `star-pivot-ui/src/views/${module}/demo`
    }
  }

  function applyBuiltinPreset(key: string) {
    if (!key || !PRESETS[key]) return
    model.value = { ...model.value, ...PRESETS[key] }
    syncFromBasePackage()
    savedPresetKey.value = undefined
  }

  function applySavedPreset(name: string) {
    if (!name) return
    const item = savedPresetList.value.find((p) => p.name === name)
    if (!item) return
    model.value = { ...item.profile }
    preset.value = undefined
  }

  function saveCurrentAsPreset() {
    const name = newPresetName.value.trim()
    if (!name) {
      ElMessage.warning('请输入预设名称')
      return
    }
    if (!model.value.basePackage?.trim()) {
      ElMessage.warning('请先填写基础包名')
      return
    }
    const idx = savedPresetList.value.findIndex((p) => p.name === name)
    const entry: SavedPreset = { name, profile: { ...model.value } }
    if (idx >= 0) {
      savedPresetList.value[idx] = entry
    } else {
      savedPresetList.value.push(entry)
    }
    persistSavedPresets()
    savedPresetKey.value = name
    newPresetName.value = ''
    ElMessage.success('预设已保存到浏览器本地')
  }

  function deleteSavedPreset() {
    const name = savedPresetKey.value
    if (!name) return
    savedPresetList.value = savedPresetList.value.filter((p) => p.name !== name)
    persistSavedPresets()
    savedPresetKey.value = undefined
    ElMessage.success('预设已删除')
  }

  function onBasePackageBlur() {
    if (!model.value.entityPackage) {
      syncFromBasePackage()
    }
  }

  function restoreLastProfile() {
    try {
      const raw = localStorage.getItem(LAST_PROFILE_KEY)
      if (raw) {
        const last = JSON.parse(raw) as GenPathProfile
        if (last.basePackage) {
          model.value = { ...model.value, ...last }
        }
      }
    } catch {
      /* ignore */
    }
  }

  watch(
    model,
    (val) => {
      if (val.basePackage) {
        localStorage.setItem(LAST_PROFILE_KEY, JSON.stringify(val))
      }
    },
    { deep: true }
  )

  onMounted(() => {
    loadSavedPresets()
    restoreLastProfile()
  })

  defineExpose({ syncFromBasePackage })
</script>

<style scoped lang="scss">
  .path-config-form {
    .section-title {
      margin: 8px 0 16px;
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .field-hint {
      margin-top: 4px;
      font-size: 12px;
      line-height: 1.4;
      color: var(--el-text-color-secondary);
    }

    .ml-2 {
      margin-left: 8px;
    }

    .ml-3 {
      margin-left: 12px;
    }
  }
</style>
