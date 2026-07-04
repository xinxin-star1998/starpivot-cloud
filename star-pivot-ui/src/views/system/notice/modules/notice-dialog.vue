<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增通知公告' : '编辑通知公告'"
    width="60%"
    align-center
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      aria-label="通知公告表单"
    >
      <ElFormItem label="公告标题" prop="noticeTitle">
        <ElInput v-model="formData.noticeTitle" placeholder="请输入公告标题" />
      </ElFormItem>
      <ElFormItem label="公告类型" prop="noticeType">
        <ElSelect v-model="formData.noticeType" placeholder="请选择公告类型">
          <!-- 字典类型：sys_notice_type，需要从字典服务获取选项 -->
          <!-- <ElOption
            v-for="item in noticeTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
            {{ item.label }}
          </ElOption> -->
          <ElOption label="通知" value="1" />
          <ElOption label="公告" value="2" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="公告状态" prop="status">
        <ElRadioGroup v-model="formData.status">
          <!-- 字典类型：sys_notice_status，需要从字典服务获取选项 -->
          <ElRadio value="0">正常</ElRadio>
          <ElRadio value="1">停用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="公告内容" prop="noticeContent">
        <art-wang-editor v-model="formData.noticeContent" />
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
import type {ElOption, FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {fetchAddNotice, fetchGetNoticeById, fetchUpdateNotice, type Notice} from '@/api/system/notice/notice'
import {DialogType} from '@/types'

interface Props {
    visible: boolean
    type: DialogType
    noticeData?: Partial<Notice>
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
    noticeTitle: '',
    noticeType: '',
    noticeContent: '',
    status: 0
  })
  // 表单验证规则
  const rules: FormRules = {
    noticeTitle: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    noticeType: [{ required: true, message: '公告类型不能为空', trigger: 'change' }]
  }
  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.noticeData

    if (isEdit && props.noticeData?.noticeId) {
      // 编辑模式：获取完整的通知公告详情
      try {
        const detail = await fetchGetNoticeById(props.noticeData.noticeId)
        console.log('通知公告详情数据:', detail)
        if (detail) {
          Object.assign(formData, {
            noticeTitle: detail.noticeTitle || '',
            noticeType: detail.noticeType || '',
            noticeContent: detail.noticeContent || '',
            status: detail.status || '0'
          })
        }
      } catch (error) {
        console.error('获取通知公告详情失败:', error)
        ElMessage.error('获取通知公告详情失败')
        // 如果获取详情失败，使用列表数据作为回退
        const row = props.noticeData
        Object.assign(formData, {
          noticeTitle: row.noticeTitle || '',
          noticeType: row.noticeType || '',
          noticeContent: row.noticeContent || '',
          status: row.status || '0'
        })
      }
    } else {
      // 新增模式：重置表单
      Object.assign(formData, {
        noticeTitle: '',
        noticeType: '',
        noticeContent: '',
        status: '0'
      })
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.noticeData],
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
          const submitData: Notice = {
            noticeTitle: formData.noticeTitle,
            noticeType: formData.noticeType,
            noticeContent: formData.noticeContent,
            status: formData.status.toString()
          }

          if (dialogType.value === 'add') {
            await fetchAddNotice(submitData)
          } else {
            submitData.noticeId = props.noticeData?.noticeId
            await fetchUpdateNotice(submitData)
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
      max-height: 60vh;
      padding: 24px;
      overflow-y: auto;
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

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: var(--art-shadow-sm);
    }
  }

  :deep(.el-select) {
    width: 100%;

    .el-select__wrapper {
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: var(--art-shadow-sm);
      }
    }
  }

  :deep(.el-radio-group) {
    .el-radio {
      margin-right: 20px;
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
