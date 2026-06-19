<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增定时任务' : '编辑定时任务'"
    width="560px"
    align-center
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      aria-label="定时任务表单"
    >
      <ElFormItem label="任务名称" prop="jobName">
        <ElInput v-model="formData.jobName" placeholder="请输入任务名称" />
      </ElFormItem>
      <ElFormItem label="任务组名" prop="jobGroup">
        <ElInput v-model="formData.jobGroup" placeholder="DEFAULT" />
      </ElFormItem>
      <ElFormItem label="调用目标" prop="invokeTarget">
        <ElInput
          v-model="formData.invokeTarget"
          type="textarea"
          :rows="2"
          placeholder="如：com.star.pivot.quartz.task.SampleTask.hello()"
        />
        <div class="form-tip"
          >格式：包名.类名.方法名()，仅允许白名单包：com.star.pivot.quartz.task</div
        >
      </ElFormItem>
      <ElFormItem label="Cron 表达式" prop="cronExpression">
        <ElInput v-model="formData.cronExpression" placeholder="如：0 0/5 * * * ? 表示每5分钟">
          <template #append>
            <ElButton type="primary" @click="handleTestCronExpression">配置</ElButton>
          </template>
        </ElInput>
      </ElFormItem>
      <ElFormItem label="执行策略" prop="misfirePolicy">
        <ElSelect v-model="formData.misfirePolicy" placeholder="请选择">
          <ElOption label="立即执行" value="1" />
          <ElOption label="执行一次" value="2" />
          <ElOption label="放弃执行" value="3" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="是否并发" prop="concurrent">
        <ElRadioGroup v-model="formData.concurrent">
          <ElRadio value="0" label="允许">允许</ElRadio>
          <ElRadio value="1" label="禁止">禁止</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio value="0" label="正常">正常</ElRadio>
          <ElRadio value="1" label="暂停">暂停</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="formData.remark" type="textarea" :rows="2" placeholder="选填" />
      </ElFormItem>
    </ElForm>

    <CronEditorDialog
      v-model="cronDialogVisible"
      :value="formData.cronExpression"
      @confirm="applyCron"
    />
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchAddJob, fetchUpdateJob, fetchJobById, type SysJob } from '@/api/monitor/job'
  import type { DialogType } from '@/types'
  import CronEditorDialog from './cron-editor-dialog.vue'

  interface Props {
    visible: boolean
    type: DialogType
    jobData?: Partial<SysJob>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  const formRef = ref<FormInstance>()
  const formData = reactive<Partial<SysJob>>({
    jobName: '',
    jobGroup: 'DEFAULT',
    invokeTarget: '',
    cronExpression: '',
    misfirePolicy: '3',
    concurrent: '1',
    status: '0',
    remark: ''
  })

  const rules: FormRules = {
    jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
    invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
    cronExpression: [{ required: true, message: '请输入 Cron 表达式', trigger: 'blur' }]
  }

  const cronDialogVisible = ref(false)

  const applyCron = (expression: string) => {
    formData.cronExpression = expression
    ElMessage.success('已应用 Cron 表达式')
  }

  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.jobData?.jobId
    Object.assign(formData, {
      jobName: '',
      jobGroup: 'DEFAULT',
      invokeTarget: '',
      cronExpression: '',
      misfirePolicy: '3',
      concurrent: '1',
      status: '0',
      remark: ''
    })
    if (isEdit && props.jobData?.jobId) {
      const res = await fetchJobById(props.jobData.jobId)
      Object.assign(formData, res)
    } else if (props.jobData) {
      Object.assign(formData, props.jobData)
    }
  }

  watch(
    () => props.visible,
    (v) => {
      if (v) {
        initFormData()
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    try {
      if (props.type === 'add') {
        await fetchAddJob(formData as SysJob)
        ElMessage.success('新增成功')
      } else {
        await fetchUpdateJob(formData as SysJob)
        ElMessage.success('修改成功')
      }
      emit('submit')
      dialogVisible.value = false
    } catch (e) {
      console.error(e)
    }
  }

  const handleTestCronExpression = () => {
    cronDialogVisible.value = true
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

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
    }
  }

  :deep(.el-select) {
    width: 100%;

    .el-select__wrapper {
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
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

  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
</style>
