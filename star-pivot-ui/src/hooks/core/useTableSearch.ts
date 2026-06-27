export function useTableSearch(
  searchParams: Record<string, unknown>,
  getData: (params?: Record<string, unknown>) => unknown
) {
  const handleSearch = (params: Record<string, unknown>) => {
    Object.assign(searchParams, params)
    return getData()
  }

  return { handleSearch }
}

export function useTableSelection<T>() {
  const selectedRows = ref<T[]>([])

  const handleSelectionChange = (selection: T[]) => {
    selectedRows.value = selection
  }

  const clearSelection = () => {
    selectedRows.value = []
  }

  return { selectedRows, handleSelectionChange, clearSelection }
}

export function useCrudDialog<T extends Record<string, unknown>>() {
  const dialogVisible = ref(false)
  const currentData = ref<Partial<T>>({})

  const openDialog = (data?: Partial<T>) => {
    currentData.value = data ?? {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const closeDialog = () => {
    dialogVisible.value = false
    currentData.value = {}
  }

  return { dialogVisible, currentData, openDialog, closeDialog }
}
