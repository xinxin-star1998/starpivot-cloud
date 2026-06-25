<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增专题' : '编辑专题'"
    width="720px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="专题名称" prop="name">
        <ElInput v-model="formData.name" maxlength="200" show-word-limit />
      </ElFormItem>
      <ElFormItem label="标题" prop="title">
        <ElInput v-model="formData.title" maxlength="255" />
      </ElFormItem>
      <ElFormItem label="副标题">
        <ElInput v-model="formData.subTitle" maxlength="255" />
      </ElFormItem>
      <ElFormItem label="专题图片" prop="img">
        <MallImageUpload v-model="formData.img" :max-size-mb="5" picker-title="选择专题图片" />
      </ElFormItem>
      <ElFormItem label="链接">
        <ElInput v-model="formData.url" placeholder="详情页链接" />
      </ElFormItem>
      <ElFormItem label="排序">
        <ElInputNumber v-model="formData.sort" :min="0" controls-position="right" />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="1">显示</ElRadio>
          <ElRadio :value="0">隐藏</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="关联商品">
        <div class="spu-list">
          <ElTable
            v-if="formData.spuList?.length"
            :data="formData.spuList"
            border
            class="spu-table"
            max-height="280px"
            size="small"
            stripe
          >
            <ElTableColumn label="ID" prop="spuId" width="72" />
            <ElTableColumn label="展示名" min-width="160">
              <template #default="{ row }">
                <ElInput v-model="row.name" placeholder="展示名" size="small" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="排序" width="100">
              <template #default="{ row }">
                <ElInputNumber
                  v-model="row.sort"
                  :min="0"
                  controls-position="right"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </ElTableColumn>
            <ElTableColumn fixed="right" label="操作" width="72">
              <template #default="{ $index }">
                <ElButton link type="danger" @click="removeSpu($index)">移除</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
          <p v-else class="spu-empty">暂未选择商品</p>
          <ElButton link type="primary" @click="spuPickerVisible = true">+ 选择商品</ElButton>
        </div>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>

  <MallSpuPickerDialog
    v-model:visible="spuPickerVisible"
    :exclude-ids="selectedSpuIds"
    @confirm="handleSpuPickerConfirm"
  />
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import {
    fetchHomeSubjectAdd,
    fetchHomeSubjectById,
    fetchHomeSubjectUpdate,
    type HomeSubjectSavePayload
  } from '@/api/mall/subject'
  import type { DialogType } from '@/types'
  import MallImageUpload from '@/components/mall/mall-image-upload/index.vue'
  import MallSpuPickerDialog, {
    type MallSpuPickerItem
  } from '@/components/mall/mall-spu-picker-dialog.vue'

  interface Props {
    visible: boolean
    type: DialogType
    subjectId?: number
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const spuPickerVisible = ref(false)

  const selectedSpuIds = computed(() =>
    (formData.value.spuList || [])
      .map((item) => item.spuId)
      .filter((id): id is number => id != null && Number.isFinite(Number(id)))
      .map((id) => Number(id))
  )

  const defaultForm = (): HomeSubjectSavePayload => ({
    name: '',
    title: '',
    subTitle: '',
    status: 1,
    sort: 0,
    img: '',
    url: '',
    spuList: []
  })

  const formData = ref<HomeSubjectSavePayload>(defaultForm())

  const rules: FormRules = {
    name: [{ required: true, message: '请输入专题名称', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = defaultForm()
      if (props.type === 'edit' && props.subjectId) {
        const detail = await fetchHomeSubjectById(props.subjectId)
        formData.value = {
          id: detail.id,
          name: detail.name || '',
          title: detail.title,
          subTitle: detail.subTitle,
          status: detail.status ?? 1,
          sort: detail.sort ?? 0,
          img: detail.img,
          url: detail.url,
          spuList: (detail.spuList || []).map((s) => ({
            spuId: s.spuId,
            name: s.name || s.spuName,
            sort: s.sort
          }))
        }
      }
    }
  )

  const handleSpuPickerConfirm = (items: MallSpuPickerItem[]) => {
    formData.value.spuList = formData.value.spuList || []
    let nextSort = formData.value.spuList.reduce((max, item) => Math.max(max, item.sort ?? 0), 0)
    for (const item of items) {
      if (selectedSpuIds.value.includes(item.spuId)) continue
      nextSort += 1
      formData.value.spuList.push({
        spuId: item.spuId,
        name: item.spuName || '',
        sort: nextSort
      })
    }
  }

  const removeSpu = (index: number) => {
    formData.value.spuList?.splice(index, 1)
  }

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      const payload = {
        ...formData.value,
        spuList: (formData.value.spuList || []).filter((s) => s.spuId)
      }
      if (props.type === 'add') {
        await fetchHomeSubjectAdd(payload)
      } else {
        await fetchHomeSubjectUpdate(payload)
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped lang="scss">
  .spu-list {
    width: 100%;
  }

  .spu-table {
    width: 100%;
    margin-bottom: 8px;
  }

  .spu-empty {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--art-gray-500);
  }
</style>
