<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增商品库存' : '编辑商品库存'"
    width="30%"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="sku_id" prop="skuId">
        <ElInput v-model="formData.skuId" placeholder="请输入sku_id" />
      </ElFormItem>
      <ElFormItem label="仓库id" prop="wareId">
        <ElInput v-model="formData.wareId" placeholder="请输入仓库id" />
      </ElFormItem>
      <ElFormItem label="库存数" prop="stock">
        <ElInput v-model="formData.stock" placeholder="请输入库存数" />
      </ElFormItem>
      <ElFormItem label="sku_name" prop="skuName">
        <ElInput v-model="formData.skuName" placeholder="请输入sku_name" />
      </ElFormItem>
      <ElFormItem label="锁定库存" prop="stockLocked">
        <ElInput v-model="formData.stockLocked" placeholder="请输入锁定库存" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { fetchAddSku, fetchGetSkuById, fetchUpdateSku, type WareSku } from '@/api/mall/ware-sku'

  interface Props {
    visible: boolean
    type: string
    skuData?: Partial<WareSku>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 表单数据
  const formData = reactive({
    skuId: 0,
    wareId: 0,
    stock: 0,
    skuName: '',
    stockLocked: 0
  })

  // 表单验证规则
  const rules: FormRules = {}

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.skuData

    if (isEdit && props.skuData?.id) {
      // 编辑模式：获取完整的商品库存详情
      try {
        const detail = await fetchGetSkuById(props.skuData.id)
        console.log('商品库存详情数据:', detail)
        if (detail) {
          Object.assign(formData, {
            skuId: detail.skuId || 0,
            wareId: detail.wareId || 0,
            stock: detail.stock || 0,
            skuName: detail.skuName || '',
            stockLocked: detail.stockLocked || 0
          })
        }
      } catch (error) {
        console.error('获取商品库存详情失败:', error)
        ElMessage.error('获取商品库存详情失败')
        // 如果获取详情失败，使用列表数据作为回退
        const row = props.skuData
        Object.assign(formData, {
          skuId: row.skuId || 0,
          wareId: row.wareId || 0,
          stock: row.stock || 0,
          skuName: row.skuName || '',
          stockLocked: row.stockLocked || 0
        })
      }
    } else {
      // 新增模式：重置表单
      Object.assign(formData, {
        skuId: 0,
        wareId: 0,
        stock: 0,
        skuName: '',
        stockLocked: 0
      })
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.skuData],
    async ([visible]) => {
      if (visible) {
        await initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   * 验证通过后触发提交事件
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const submitData: any = {
            skuId: formData.skuId,
            wareId: formData.wareId,
            stock: formData.stock,
            skuName: formData.skuName,
            stockLocked: formData.stockLocked
          }

          if (dialogType.value === 'add') {
            await fetchAddSku(submitData)
          } else {
            submitData.id = props.skuData?.id
            await fetchUpdateSku(submitData)
          }
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('提交失败:', error)
          ElMessage.error(dialogType.value === 'add' ? '新增失败' : '更新失败')
        }
      }
    })
  }
</script>

<style scoped lang="scss">
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
</style>
