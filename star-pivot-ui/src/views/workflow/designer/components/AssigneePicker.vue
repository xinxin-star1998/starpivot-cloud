<script setup lang="ts">
  import { fetchGetPostSelect } from '@/api/post/post'
  import { fetchGetRoleSelect } from '@/api/role/role'
  import { fetchGetUserList } from '@/api/user/user'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type { AssigneeRule, AssigneeRuleType } from '../../utils/flow-types'
  import { ASSIGNEE_RULE_OPTIONS } from '../../utils/flow-types'

  const model = defineModel<AssigneeRule | undefined>({ required: true })

  type RoleOption = Api.SystemManage.RoleListItem
  type PostOption = Api.Post.PostBo
  type UserOption = Api.SystemManage.UserListItem

  const roleList = ref<RoleOption[]>([])
  const postList = ref<PostOption[]>([])
  const userList = ref<UserOption[]>([])
  const userLoading = ref(false)

  const assigneeType = computed({
    get: () => model.value?.type,
    set: (type: AssigneeRuleType) => {
      model.value = { type, value: '' }
    }
  })

  const assigneeValue = computed({
    get: () => (model.value?.value != null ? String(model.value.value) : ''),
    set: (val: string) => {
      if (!model.value) return
      model.value = { ...model.value, value: val }
    }
  })

  const typeLabel = computed(
    () => ASSIGNEE_RULE_OPTIONS.find((o) => o.value === assigneeType.value)?.label
  )

  onMounted(async () => {
    const [roles, posts] = await Promise.all([fetchGetRoleSelect(), fetchGetPostSelect()])
    roleList.value = Array.isArray(roles) ? roles : []
    const postData = posts as PostOption | PostOption[]
    postList.value = Array.isArray(postData) ? postData : postData ? [postData] : []
    await loadUsers()
  })

  async function loadUsers(keyword = '') {
    userLoading.value = true
    try {
      const res = await fetchGetUserList({
        pageNum: 1,
        pageSize: 50,
        userName: keyword || undefined
      })
      userList.value = res.records || []
    } finally {
      userLoading.value = false
    }
  }
</script>

<template>
  <div class="assignee-picker">
    <div class="assignee-picker__field">
      <label class="assignee-picker__label">
        <ArtSvgIcon icon="ri:shield-user-line" />
        规则类型
      </label>
      <ElSelect v-model="assigneeType" placeholder="选择审批人规则" class="assignee-picker__select">
        <ElOption
          v-for="item in ASSIGNEE_RULE_OPTIONS"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </ElSelect>
    </div>

    <div v-if="assigneeType === 'ROLE'" class="assignee-picker__field">
      <label class="assignee-picker__label">
        <ArtSvgIcon icon="ri:group-line" />
        选择角色
      </label>
      <ElSelect
        v-model="assigneeValue"
        filterable
        placeholder="选择角色"
        class="assignee-picker__select"
      >
        <ElOption
          v-for="role in roleList"
          :key="role.roleId"
          :label="role.roleName"
          :value="role.roleKey"
        />
      </ElSelect>
    </div>

    <div v-else-if="assigneeType === 'POST'" class="assignee-picker__field">
      <label class="assignee-picker__label">
        <ArtSvgIcon icon="ri:briefcase-line" />
        选择岗位
      </label>
      <ElSelect
        v-model="assigneeValue"
        filterable
        placeholder="选择岗位"
        class="assignee-picker__select"
      >
        <ElOption
          v-for="post in postList"
          :key="post.postId"
          :label="post.postName"
          :value="post.postCode"
        />
      </ElSelect>
    </div>

    <div v-else-if="assigneeType === 'USER'" class="assignee-picker__field">
      <label class="assignee-picker__label">
        <ArtSvgIcon icon="ri:user-search-line" />
        选择用户
      </label>
      <ElSelect
        v-model="assigneeValue"
        filterable
        remote
        :remote-method="loadUsers"
        :loading="userLoading"
        placeholder="搜索并选择用户"
        class="assignee-picker__select"
      >
        <ElOption
          v-for="user in userList"
          :key="user.userId"
          :label="`${user.nickName || user.userName} (${user.userName})`"
          :value="String(user.userId)"
        />
      </ElSelect>
    </div>

    <div v-else-if="assigneeType && typeLabel" class="assignee-picker__hint">
      <ArtSvgIcon icon="ri:information-line" />
      已选择「{{ typeLabel }}」，无需额外配置
    </div>
  </div>
</template>

<style scoped lang="scss">
  .assignee-picker {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .assignee-picker__field {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .assignee-picker__label {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    font-weight: 500;
    color: var(--el-text-color-secondary);
  }

  .assignee-picker__select {
    width: 100%;
  }

  .assignee-picker__hint {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 10px;
    border-radius: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-extra-light);
  }
</style>
