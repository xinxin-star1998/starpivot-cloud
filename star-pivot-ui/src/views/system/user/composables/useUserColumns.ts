import type {Ref} from 'vue'
import type {ColumnOption} from '@/types/component'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtAvatarDisplay from '@/components/core/media/art-avatar-display/index.vue'
import {ElSwitch} from 'element-plus'
import type {DialogType} from '@/types'

type UserListItem = Api.SystemManage.UserListItem

export interface UserColumnHandlers {
  hasAuth: (perm: string) => boolean
  currentUserId: Ref<number | undefined>
  showDialog: (type: DialogType, row?: UserListItem) => void
  deleteUser: (row: UserListItem) => void
  unlockUser: (row: UserListItem) => void
  resetPwd: (row: UserListItem) => void
  handleStatusChange: (row: UserListItem, value: boolean) => void
}

export function createUserColumns(handlers: UserColumnHandlers): ColumnOption<UserListItem>[] {
  const {
    hasAuth,
    currentUserId,
    showDialog,
    deleteUser,
    unlockUser,
    resetPwd,
    handleStatusChange
  } = handlers

  return [
    { type: 'selection' },
    { type: 'index', width: 60, label: '序号' },
    {
      prop: 'userInfo',
      label: '用户信息',
      width: 300,
      formatter: (row) => {
        const user = row as UserListItem
        const avatarUrl = user.avatar || ''
        const hasAvatar = !!avatarUrl && avatarUrl !== ''

        return h('div', { class: 'user-info flex-c items-center' }, [
          hasAvatar &&
            h('div', { class: 'avatar-wrapper' }, [
              h(ArtAvatarDisplay, {
                avatarUrl,
                size: 40,
                avatarClass: 'size-10'
              })
            ]),
          h(
            'div',
            {
              class: `flex-1 min-w-0 ${hasAvatar ? 'ml-3' : ''}`
            },
            [
              h(
                'div',
                {
                  class: 'flex items-center gap-2',
                  style: { whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }
                },
                [
                  h(
                    'span',
                    {
                      class: 'user-name font-medium',
                      style: {
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        color: 'var(--art-gray-900)'
                      }
                    },
                    user.userName || '未知用户'
                  ),
                  h('span', {
                    class: 'status-indicator',
                    style: {
                      display: 'inline-block',
                      width: '8px',
                      height: '8px',
                      borderRadius: '50%',
                      backgroundColor:
                        user.status === '0' ? 'var(--el-color-success)' : 'var(--el-color-info)'
                    }
                  })
                ]
              ),
              h(
                'p',
                {
                  class: 'email text-sm',
                  style: {
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    color: 'var(--art-gray-500)'
                  }
                },
                user.email || '无邮箱'
              )
            ]
          )
        ])
      }
    },
    {
      prop: 'deptNameText',
      label: '所属部门',
      sortable: true,
      minWidth: 140,
      showOverflowTooltip: true
    },
    {
      prop: 'roleNamesText',
      label: '关联角色',
      minWidth: 180,
      showOverflowTooltip: true
    },
    {
      prop: 'postNamesText',
      label: '所属岗位',
      minWidth: 180,
      showOverflowTooltip: true
    },
    {
      prop: 'sex',
      label: '性别',
      sortable: true,
      formatter: (row) => {
        const user = row as UserListItem
        const sexMap: Record<string, string> = {
          '0': '男',
          '1': '女',
          '2': '未知'
        }
        return sexMap[user.sex] || user.sex || '未知'
      }
    },
    {
      prop: 'phonenumber',
      label: '手机号',
      formatter: (row) => {
        const user = row as UserListItem
        return user.phonenumber || '未知'
      }
    },
    {
      prop: 'status',
      label: '状态',
      formatter: (row) => {
        const user = row as UserListItem
        return h(ElSwitch, {
          modelValue: user.status === '0',
          activeValue: true,
          inactiveValue: false,
          onChange: (value: string | number | boolean) => {
            handleStatusChange(user, value === true)
          }
        })
      }
    },
    {
      prop: 'createTime',
      label: '创建日期',
      sortable: true
    },
    {
      prop: 'operation',
      label: '操作',
      width: 180,
      fixed: 'right',
      formatter: (row) => {
        const user = row as UserListItem
        const actions: any[] = []

        if (hasAuth('system:user:edit')) {
          actions.push(
            h(ArtButtonTable, {
              type: 'edit',
              tooltip: '编辑用户',
              onClick: () => showDialog('edit', user)
            })
          )

          if (user.isLocked === true) {
            actions.push(
              h(ArtButtonTable, {
                icon: 'ri:lock-unlock-line',
                iconClass: 'bg-warning/12 text-warning',
                tooltip: '解锁账户',
                onClick: () => unlockUser(user)
              })
            )
          }
        }

        if (hasAuth('system:user:delete')) {
          actions.push(
            h(ArtButtonTable, {
              type: 'delete',
              tooltip: '删除用户',
              onClick: () => deleteUser(user)
            })
          )
        }

        if (hasAuth('system:user:resetPwd') && user.userId !== currentUserId.value) {
          actions.push(
            h(ArtButtonTable, {
              type: 'sync',
              tooltip: '重置密码',
              onClick: () => resetPwd(user)
            })
          )
        }

        if (actions.length === 0) {
          return h('span', { style: 'color: var(--art-gray-500)' }, '')
        }

        return h('div', actions)
      }
    }
  ]
}

export function transformUserListRecords(records: UserListItem[]): UserListItem[] {
  if (!Array.isArray(records)) {
    return []
  }

  return records.map((user) => {
    const roleNames = Array.isArray(user.roleNames) ? user.roleNames.filter(Boolean) : []
    const postNames = Array.isArray(user.postNames) ? user.postNames.filter(Boolean) : []
    return {
      ...user,
      deptNameText: user.deptName || '-',
      roleNamesText: roleNames.length ? roleNames.join('、') : '-',
      postNamesText: postNames.length ? postNames.join('、') : '-'
    }
  })
}
