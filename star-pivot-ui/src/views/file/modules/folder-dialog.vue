<template>
  <ElDialog
    v-model="visible"
    :title="isEdit ? '编辑文件夹' : '新建文件夹'"
    width="480px"
    destroy-on-close
    @closed="reset"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="90px">
      <ElFormItem v-if="!isEdit" label="业务分类" prop="category">
        <ElSelect v-model="form.category" placeholder="请选择分类" style="width: 100%">
          <ElOption
            v-for="item in FILE_CATEGORIES"
            :key="item.code"
            :label="item.label"
            :value="item.code"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="文件夹名" prop="folderName">
        <ElInput v-model="form.folderName" placeholder="请输入文件夹名称" />
      </ElFormItem>
      <ElFormItem label="排序">
        <ElInputNumber v-model="form.orderNum" :min="0" />
      </ElFormItem>
      <ElFormItem label="备注">
        <ElInput v-model="form.remark" type="textarea" :rows="2" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="submit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {createFolder, updateFolder} from '@/api/file/folder'
import type {SysFileFolderForm} from '@/api/file/types'
import {FILE_CATEGORIES} from '../constants'
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {computed, reactive, ref, watch} from 'vue'

const visible = defineModel<boolean>('visible', { default: false })

  const props = defineProps<{
    type: 'add' | 'edit'
    data?: SysFileFolderForm
    defaultCategory?: string
  }>()

  const emit = defineEmits<{
    success: []
  }>()

  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const form = reactive<SysFileFolderForm>({
    category: '',
    folderName: '',
    orderNum: 0,
    remark: ''
  })

  const isEdit = computed(() => props.type === 'edit')

  const rules: FormRules = {
    category: [{ required: true, message: '请选择业务分类', trigger: 'change' }],
    folderName: [{ required: true, message: '请输入文件夹名称', trigger: 'blur' }]
  }

  watch(
    () => visible.value,
    (open) => {
      if (!open) return
      if (props.type === 'edit' && props.data) {
        Object.assign(form, props.data)
      } else {
        reset()
        form.category = props.defaultCategory || ''
      }
    }
  )

  function reset() {
    form.folderId = undefined
    form.category = props.defaultCategory || ''
    form.folderName = ''
    form.orderNum = 0
    form.remark = ''
  }

  async function submit() {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateFolder({ ...form })
        ElMessage.success('更新成功')
      } else {
        await createFolder({ ...form })
        ElMessage.success('创建成功')
      }
      visible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
