<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
    width="40%"
    align-center
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
      aria-label="用户信息表单"
    >
      <ElFormItem label="用户名" prop="username">
        <ElInput v-model="formData.userName" placeholder="请输入用户名" />
      </ElFormItem>
      <ElFormItem label="用户密码" prop="username" v-if="dialogType === 'add'">
        <ElInput v-model="formData.password" placeholder="请输入用户密码" />
      </ElFormItem>
      <ElFormItem label="用户昵称" prop="nickName">
        <ElInput v-model="formData.nickName" placeholder="请输入用户昵称" />
      </ElFormItem>
      <ElFormItem label="邮箱" prop="email">
        <ElInput v-model="formData.email" placeholder="请输入邮箱" />
      </ElFormItem>

      <ElFormItem label="头像">
        <art-avatar-upload
          ref="avatarUploadRef"
          v-model="formData.avatar"
          :user-id="formData.userId"
          :size="100"
          :auto-upload="dialogType === 'edit'"
          use-presigned-url
        />
      </ElFormItem>

      <ElFormItem label="手机号" prop="phone">
        <ElInput v-model="formData.phonenumber" placeholder="请输入手机号" />
      </ElFormItem>
      <ElFormItem label="性别" prop="gender">
        <ElRadioGroup v-model="formData.sex">
          <ElRadio :value="'0'">男</ElRadio>
          <ElRadio :value="'1'">女</ElRadio>
          <ElRadio :value="'2'">未知</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="'0'">启用</ElRadio>
          <ElRadio :value="'1'">禁用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="角色" prop="role">
        <ElSelect v-model="formData.roleIds" multiple>
          <ElOption
            v-for="role in roleList"
            :key="role.roleCode"
            :value="role.roleCode"
            :label="role.roleName"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="岗位" prop="post">
        <ElSelect v-model="formData.postIds" multiple>
          <ElOption
            v-for="post in postList"
            :key="post.postId"
            :value="post.postId?.toString()"
            :label="post.postName"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="部门" prop="deptId">
        <ElTreeSelect
          v-model="formData.deptId"
          :data="deptTreeData"
          :props="deptTreeProps"
          placeholder="请选择部门"
          clearable
          check-strictly
          :render-after-expand="false"
        />
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput type="textarea" v-model="formData.remark" placeholder="请输入备注" />
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
  import { ElTreeSelect } from 'element-plus'
  import { useUserStore } from '@/store/modules/user'
  import { fetchGetRoleSelect } from '@/api/role/role'
  import { fetchGetPostSelect } from '@/api/post/post'
  import { fetchGetDeptTree, type SysDept } from '@/api/dept/dept'
  import { fetchAddUser, fetchUpdateUser, fetchGetUserById } from '@/api/user/user'
  import ArtAvatarUpload from '@/components/core/media/art-avatar-upload/index.vue'

  // 角色列表项类型（扩展 RoleListItem，添加 roleCode 字段）
  type RoleOption = Api.SystemManage.RoleListItem & { roleCode: string }

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserBo>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const userStore = useUserStore()

  // 角色列表数据
  const roleList = ref<RoleOption[]>([])
  // 岗位列表数据
  const postList = ref<Api.Post.PostBo[]>([])
  // 部门树数据
  const deptTreeData = ref<SysDept[]>([])
  // 部门树配置
  const deptTreeProps = {
    value: 'deptId',
    label: 'deptName',
    children: 'children'
  }
  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()
  // 头像上传组件实例
  const avatarUploadRef = ref<any>()

  // 表单数据
  const formData = reactive({
    userId: 0,
    deptId: 0,
    userName: '',
    nickName: '',
    email: '',
    avatar: '',
    password: '',
    phonenumber: '',
    status: '0',
    sex: '0',
    remark: '',
    roleIds: [] as string[],
    postIds: [] as string[]
  })

  // 表单验证规则
  const rules: FormRules = {
    userName: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    phonenumber: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    sex: [{ required: true, message: '请选择性别', trigger: 'blur' }],
    roleIds: [{ required: true, message: '请选择角色', trigger: 'blur' }]
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.userData
    const row = props.userData

    if (isEdit && row?.userId) {
      // 编辑模式：获取完整的用户详情（包含 roleIds 和 postIds）
      try {
        const userDetail = await fetchGetUserById(row.userId)
        if (userDetail) {
          // 处理角色ID：可能是 roleIds 数组，也可能是 userRoles 数组
          let roleIdsArray: any[] = []
          if (Array.isArray((userDetail as any).roleIds)) {
            roleIdsArray = (userDetail as any).roleIds
          } else if (Array.isArray((userDetail as any).userRoles)) {
            // 如果后端返回的是 userRoles，提取角色ID
            roleIdsArray = (userDetail as any).userRoles.map((role: any) =>
              typeof role === 'object' ? role.roleId : role
            )
          }

          // 转换为字符串数组，与 roleCode 格式一致
          const roleIds = roleIdsArray.map((id: any) => id?.toString() || '').filter(Boolean)

          // 处理岗位ID
          const postIds = Array.isArray((userDetail as any).postIds)
            ? (userDetail as any).postIds.map((id: any) => id?.toString() || '').filter(Boolean)
            : []

          Object.assign(formData, {
            userId: userDetail.userId || 0,
            deptId: userDetail.deptId || 0,
            userName: userDetail.userName || '',
            nickName: userDetail.nickName || '',
            email: userDetail.email || '',
            avatar: userDetail.avatar || '',
            phonenumber: userDetail.phonenumber || '',
            sex: userDetail.sex || '0',
            status: userDetail.status || '0',
            password: '',
            roleIds,
            postIds,
            remark: userDetail.remark || ''
          })
        }
      } catch (error) {
        console.error('获取用户详情失败:', error)
        ElMessage.error('获取用户详情失败')
        // 如果获取详情失败，使用列表数据作为回退
        Object.assign(formData, {
          userId: row.userId || 0,
          deptId: row.deptId || 0,
          userName: row.userName || '',
          nickName: row.nickName || '',
          email: row.email || '',
          avatar: row.avatar || '',
          phonenumber: row.phonenumber || '',
          sex: row.sex || '0',
          status: row.status || '0',
          password: '',
          remark: row.remark || '',
          roleIds: [],
          postIds: []
        })
      }
    } else {
      // 新增模式：重置表单
      Object.assign(formData, {
        userId: 0,
        deptId: 0,
        userName: '',
        nickName: '',
        email: '',
        phonenumber: '',
        sex: '0',
        status: '0',
        password: '',
        roleIds: [],
        postIds: []
      })
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.userData],
    async ([visible]) => {
      if (visible) {
        // 先获取下拉数据
        await Promise.all([
          getRoleList(), // 获取角色列表
          getPostList(), // 获取岗位列表
          getDeptTree() // 获取部门树
        ])
        // 再初始化表单数据（编辑模式会获取用户详情）
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
          if (dialogType.value === 'add') {
            // 新增用户
            const addResult = (await fetchAddUser(formData)) as any
            // 假设后端返回的结果包含userId
            const newUserId = addResult?.userId || addResult?.id

            // 设置新的userId到formData，以便上传头像时使用
            if (newUserId) {
              formData.userId = newUserId
              // 调用头像上传组件的手动上传方法
              if (avatarUploadRef.value) {
                await avatarUploadRef.value.uploadImageToServer()
              }
            }
          } else {
            // 编辑用户，不传密码字段
            const { password: _, ...updateData } = formData
            await fetchUpdateUser(updateData)
            // 编辑时如果修改了头像，autoUpload为true会自动上传
            // 若编辑的是当前登录用户，同步头像到 store，便于顶部下拉立即展示最新头像
            const cur = userStore.getUserInfo as any
            const curId = cur?.userId ?? cur?.user?.userId
            if (curId != null && String(formData.userId) === String(curId)) {
              userStore.setUserInfo({
                avatar: formData.avatar,
                avatarUpdatedAt: Date.now()
              })
            }
          }

          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('提交失败:', error)
          ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
        }
      }
    })
  }
  /**
   * 获取角色下拉数据
   */
  const getRoleList = async () => {
    try {
      const res = await fetchGetRoleSelect()
      if (Array.isArray(res) && res.length > 0) {
        // 将 roleKey 映射为 roleCode，以匹配模板中的使用
        roleList.value = res.map((role) => ({
          ...role,
          roleCode: role.roleId?.toString()
        }))
      } else {
        roleList.value = []
      }
    } catch (error) {
      console.error('获取角色列表失败:', error)
      roleList.value = []
      ElMessage.error('获取角色列表失败')
    }
  }
  /**
   * 获取岗位下拉数据
   */
  const getPostList = async () => {
    try {
      const res = await fetchGetPostSelect()
      if (Array.isArray(res) && res.length > 0) {
        postList.value = res
      } else {
        postList.value = []
      }
    } catch (error) {
      console.error('获取岗位列表失败:', error)
      postList.value = []
      ElMessage.error('获取岗位列表失败')
    }
  }
  /**
   * 获取部门树数据
   */
  const getDeptTree = async () => {
    try {
      const res = await fetchGetDeptTree()
      if (Array.isArray(res) && res.length > 0) {
        deptTreeData.value = res
      } else {
        deptTreeData.value = []
      }
    } catch (error) {
      console.error('获取部门树失败:', error)
      deptTreeData.value = []
      ElMessage.error('获取部门树失败')
    }
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

  :deep(.el-tree-select) {
    width: 100%;
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
