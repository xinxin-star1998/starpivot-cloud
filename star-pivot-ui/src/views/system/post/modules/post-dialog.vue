<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增岗位' : '编辑岗位'"
    width="30%"
    align-center
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      aria-label="岗位信息表单"
    >
      <ElFormItem label="岗位编码" prop="postCode">
        <ElInput v-model="formData.postCode" placeholder="请输入岗位编码" />
      </ElFormItem>
      <ElFormItem label="岗位名称" prop="postName">
        <ElInput v-model="formData.postName" placeholder="请输入岗位名称" />
      </ElFormItem>
      <ElFormItem label="显示顺序" prop="postSort">
        <ElInputNumber
          v-model="formData.postSort"
          :min="0"
          :max="999"
          placeholder="请输入显示顺序"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="'0'">正常</ElRadio>
          <ElRadio :value="'1'">停用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput type="textarea" v-model="formData.remark" :rows="4" placeholder="请输入备注" />
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
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchAddPost, fetchUpdatePost, fetchGetPostById, type SysPost } from '@/api/post/post'

  interface Props {
    visible: boolean
    type: string
    postData?: Partial<Api.Post.PostListItem>
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
    postId: 0,
    postCode: '',
    postName: '',
    postSort: 0,
    status: '0',
    remark: ''
  })

  // 表单验证规则
  const rules: FormRules = {
    postCode: [
      { required: true, message: '请输入岗位编码', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    postName: [
      { required: true, message: '请输入岗位名称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    postSort: [{ required: true, message: '请输入显示顺序', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.postData

    if (isEdit && props.postData?.postId) {
      // 编辑模式：获取完整的岗位详情
      try {
        const postDetail = await fetchGetPostById(props.postData.postId)
        if (postDetail) {
          Object.assign(formData, {
            postId: postDetail.postId || 0,
            postCode: postDetail.postCode || '',
            postName: postDetail.postName || '',
            postSort: postDetail.postSort || 0,
            status: postDetail.status !== undefined ? postDetail.status.toString() : '0',
            remark: postDetail.remark || ''
          })
        }
      } catch (error) {
        console.error('获取岗位详情失败:', error)
        ElMessage.error('获取岗位详情失败')
        // 如果获取详情失败，使用列表数据作为回退
        const row = props.postData
        Object.assign(formData, {
          postId: row.postId || 0,
          postCode: row.postCode || '',
          postName: row.postName || '',
          postSort: row.postSort || 0,
          status: row.status !== undefined ? row.status.toString() : '0',
          remark: row.remark || ''
        })
      }
    } else {
      // 新增模式：重置表单
      Object.assign(formData, {
        postId: 0,
        postCode: '',
        postName: '',
        postSort: 0,
        status: '0',
        remark: ''
      })
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.postData],
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
          const submitData: SysPost = {
            postCode: formData.postCode,
            postName: formData.postName,
            postSort: formData.postSort,
            status: formData.status,
            remark: formData.remark
          }

          if (dialogType.value === 'add') {
            await fetchAddPost(submitData)
          } else {
            submitData.postId = formData.postId
            await fetchUpdatePost(submitData)
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
      box-shadow: var(--art-shadow-sm);
    }
  }

  :deep(.el-input-number) {
    width: 100%;

    .el-input__wrapper {
      border-radius: 8px;
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
