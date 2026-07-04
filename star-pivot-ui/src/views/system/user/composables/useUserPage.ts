import {fetchGetUserList} from '@/api/user/user'
import {useTable} from '@/hooks/core/useTable'
import {useTableSearch, useTableSelection} from '@/hooks/core/useTableSearch'
import {useAuth} from '@/hooks/core/useAuth'
import {useUserStore} from '@/store/modules/user'
import {DialogType} from '@/types'
import {createUserColumns, transformUserListRecords} from './useUserColumns'
import {useUserActions} from './useUserActions'

type UserListItem = Api.SystemManage.UserListItem

export function useUserPage() {
  const { hasAuth } = useAuth()
  const userStore = useUserStore()
  const currentUserId = computed(() => {
    const info = userStore.getUserInfo as Record<string, unknown>
    const user = info?.user as Record<string, unknown> | undefined
    return (info?.userId ?? user?.userId) as number | undefined
  })

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentUserData = ref<Partial<UserListItem>>({})
  const importDialogVisible = ref(false)
  const deptPanelCollapsed = ref(false)

  const searchForm = ref<{
    userName: string | undefined
    sex: string | undefined
    phonenumber: string | undefined
    email: string | undefined
    status: string
    deptId: number | undefined
  }>({
    userName: undefined,
    sex: undefined,
    phonenumber: undefined,
    email: undefined,
    status: '0',
    deptId: undefined
  })

  const { selectedRows, handleSelectionChange } = useTableSelection<UserListItem>()

  const showDialog = (type: DialogType, row?: UserListItem): void => {
    dialogType.value = type
    currentUserData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  let deleteUserFn!: (row: UserListItem) => Promise<void>
  let unlockUserFn!: (row: UserListItem) => Promise<void>
  let resetPwdFn!: (row: UserListItem) => Promise<void>
  let handleStatusChangeFn!: (row: UserListItem, value: boolean) => Promise<void>

  const table = useTable({
    core: {
      apiFn: (params: any) => fetchGetUserList(params as Api.SystemManage.UserSearchParams),
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () =>
        createUserColumns({
          hasAuth,
          currentUserId,
          showDialog,
          deleteUser: (row) => deleteUserFn(row),
          unlockUser: (row) => unlockUserFn(row),
          resetPwd: (row) => resetPwdFn(row),
          handleStatusChange: (row, value) => handleStatusChangeFn(row, value)
        })
    },
    transform: {
      dataTransformer: transformUserListRecords
    }
  })

  const actions = useUserActions({
    selectedRows,
    searchForm,
    refreshData: table.refreshData
  })

  deleteUserFn = actions.deleteUser
  unlockUserFn = actions.unlockUser
  resetPwdFn = actions.resetPwd
  handleStatusChangeFn = actions.handleStatusChange

  const { handleSearch } = useTableSearch(table.searchParams, table.getData)

  const handleDeptSelect = (deptId: number | undefined) => {
    searchForm.value.deptId = deptId
    handleSearch(searchForm.value)
  }

  const handleDialogSubmit = async () => {
    dialogVisible.value = false
    currentUserData.value = {}
    await table.refreshData()
  }

  return {
    searchForm,
    deptPanelCollapsed,
    dialogType,
    dialogVisible,
    currentUserData,
    importDialogVisible,
    selectedRows,
    handleSelectionChange,
    showDialog,
    handleSearch,
    handleDeptSelect,
    handleDialogSubmit,
    ...table,
    ...actions
  }
}
