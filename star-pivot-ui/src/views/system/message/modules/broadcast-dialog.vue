<template>
  <ElDialog v-model="visible" destroy-on-close title="群发站内消息" width="560px">
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="88px">
      <ElFormItem label="标题" prop="title">
        <ElInput v-model="form.title" maxlength="200" show-word-limit />
      </ElFormItem>
      <ElFormItem label="内容" prop="content">
        <ElInput v-model="form.content" :rows="4" maxlength="500" show-word-limit type="textarea" />
      </ElFormItem>
      <ElFormItem label="发送范围" prop="targetType">
        <ElRadioGroup v-model="form.targetType">
          <ElRadio value="ALL">全部用户</ElRadio>
          <ElRadio value="ROLE">按角色</ElRadio>
          <ElRadio value="USER">指定用户</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem v-if="form.targetType === 'ROLE'" label="角色" prop="roleIds">
        <ElSelect v-model="form.roleIds" filterable multiple placeholder="选择角色" style="width: 100%">
          <ElOption
            v-for="role in roleOptions"
            :key="role.roleId"
            :label="role.roleName"
            :value="role.roleId"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem v-if="form.targetType === 'USER'" label="接收用户" prop="userIds">
        <div class="user-picker">
          <ElButton type="primary" plain @click="userSelectVisible = true">选择用户</ElButton>
          <span v-if="selectedUsers.length" class="picker-hint">已选 {{ selectedUsers.length }} 人</span>
          <div v-if="selectedUsers.length" class="selected-tags">
            <ElTag
              v-for="user in selectedUsers"
              :key="user.userId"
              closable
              @close="removeUser(user.userId)"
            >
              {{ user.nickName || user.userName || `用户#${user.userId}` }}
            </ElTag>
          </div>
        </div>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :loading="submitting" type="primary" @click="handleSubmit">发送</ElButton>
    </template>

    <UserSelectDialog
      v-model:visible="userSelectVisible"
      :selected="selectedUsers"
      @confirm="handleUsersSelected"
    />
  </ElDialog>
</template>

<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import { fetchMessageBroadcast } from '@/api/system/message'
import { fetchGetRoleSelect } from '@/api/role/role'
import UserSelectDialog, { type SelectedUserItem } from './user-select-dialog.vue'

const visible = defineModel<boolean>('visible', { default: false })
const emit = defineEmits<{ success: [] }>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const userSelectVisible = ref(false)
const roleOptions = ref<Api.SystemManage.RoleListItem[]>([])
const selectedUsers = ref<SelectedUserItem[]>([])

const form = reactive({
  title: '',
  content: '',
  targetType: 'ALL' as 'ALL' | 'ROLE' | 'USER',
  roleIds: [] as number[],
  userIds: [] as number[]
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  targetType: [{ required: true, message: '请选择发送范围', trigger: 'change' }],
  roleIds: [{
    validator: (_rule, _value, callback) => {
      if (form.targetType === 'ROLE' && !form.roleIds.length) {
        callback(new Error('请选择至少一个角色'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],
  userIds: [{
    validator: (_rule, _value, callback) => {
      if (form.targetType === 'USER' && !form.userIds.length) {
        callback(new Error('请选择至少一名用户'))
        return
      }
      callback()
    },
    trigger: 'change'
  }]
}

function handleUsersSelected(users: SelectedUserItem[]) {
  selectedUsers.value = users
  form.userIds = users.map((item) => item.userId)
}

function removeUser(userId: number) {
  selectedUsers.value = selectedUsers.value.filter((item) => item.userId !== userId)
  form.userIds = selectedUsers.value.map((item) => item.userId)
}

watch(visible, async (open) => {
  if (!open) return
  form.title = ''
  form.content = ''
  form.targetType = 'ALL'
  form.roleIds = []
  form.userIds = []
  selectedUsers.value = []
  if (!roleOptions.value.length) {
    roleOptions.value = (await fetchGetRoleSelect()) || []
  }
})

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await fetchMessageBroadcast({
      title: form.title,
      content: form.content,
      targetType: form.targetType,
      roleIds: form.targetType === 'ROLE' ? form.roleIds : undefined,
      userIds: form.targetType === 'USER' ? form.userIds : undefined
    })
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.user-picker {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.picker-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
