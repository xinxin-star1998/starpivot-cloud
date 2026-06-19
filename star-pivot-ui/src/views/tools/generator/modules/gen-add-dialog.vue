<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '创建表' : '编辑表'"
    width="60%"
    align-center
    @closed="handleClosed"
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="140px"
      label-position="top"
      aria-label="建表SQL表单"
    >
      <ElFormItem label="创建表语句(支持多个建表语句)：" prop="tableSql">
        <ElInput
          v-model="formData.tableSql"
          type="textarea"
          :rows="12"
          placeholder="请输入建表SQL语句，支持多个建表语句，用分号分隔&#10;例如：&#10;CREATE TABLE sys_user (&#10;  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,&#10;  username VARCHAR(50) NOT NULL,&#10;  password VARCHAR(100) NOT NULL&#10;) COMMENT='用户表';"
          clearable
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleCancel">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitting">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus'
  import { DialogType } from '@/types'
  import { fetchCreateTable } from '@/api/generator/gen-table'

  interface Props {
    visible: boolean
    type: DialogType
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', data: { tableSql: string }): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value: boolean) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 提交状态
  const submitting = ref(false)

  // 表单数据
  const formData = reactive({
    tableSql: ''
  })

  // 表单验证规则
  const rules: FormRules = {
    tableSql: [
      { required: true, message: '请输入建表SQL语句', trigger: 'blur' },
      {
        validator: (rule: any, value: string, callback: (error?: Error) => void) => {
          if (!value || value.trim() === '') {
            callback(new Error('请输入建表SQL语句'))
            return
          }
          // 简单的SQL验证：检查是否包含CREATE TABLE关键字
          const sqlUpper = value.toUpperCase().trim()
          if (!sqlUpper.includes('CREATE TABLE')) {
            callback(new Error('请输入有效的CREATE TABLE语句'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }

  /**
   * 重置表单数据
   */
  const resetForm = (): void => {
    Object.assign(formData, {
      tableSql: ''
    })
    nextTick(() => {
      formRef.value?.clearValidate()
    })
  }

  /**
   * 提交表单
   * 验证通过后触发提交事件
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()
      submitting.value = true
      await fetchCreateTable(formData.tableSql.trim())
      ElMessage.success('创建表成功')
      dialogVisible.value = false
      emit('submit', { tableSql: formData.tableSql.trim() })
    } catch (error) {
      console.error('创建表失败:', error)
      ElMessage.error('创建表失败，请检查SQL语句是否正确')
    } finally {
      submitting.value = false
    }
  }

  /**
   * 取消操作
   */
  const handleCancel = (): void => {
    dialogVisible.value = false
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
        await nextTick()
        resetForm()
      }
    }
  )
</script>

<style scoped lang="scss">
  :deep(.el-dialog) {
    overflow: hidden;
    border-radius: 16px;

    .el-dialog__header {
      padding: 20px 24px;
      margin: 0;
      background: linear-gradient(
        135deg,
        var(--el-color-primary-light-9) 0%,
        var(--el-color-primary-light-8) 100%
      );
      border-bottom: 1px solid var(--art-card-border);

      .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        color: var(--art-gray-900);
      }
    }

    .el-dialog__body {
      padding: 24px;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      background-color: var(--art-gray-50);
      border-top: 1px solid var(--art-card-border);
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--art-gray-700);
  }

  :deep(.el-textarea__inner) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
    }
  }

  :deep(.el-button) {
    padding: 10px 24px;
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }

  .dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }
</style>
