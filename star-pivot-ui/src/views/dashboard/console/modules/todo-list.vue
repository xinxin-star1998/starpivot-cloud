<template>
  <div class="art-card h-128 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>{{ t('dashboard.todoList.title') }}</h4>
        <p
          >{{ t('dashboard.todoList.pending')
          }}<span class="text-danger">{{ pendingCount }}</span></p
        >
      </div>
    </div>

    <div class="h-[calc(100%-40px)] overflow-auto">
      <ElScrollbar>
        <div
          class="flex-cb h-17.5 border-b border-g-300 text-sm last:border-b-0"
          v-for="(item, index) in list"
          :key="index"
        >
          <div>
            <p class="text-sm">{{ item.username }}</p>
            <p class="text-g-500 mt-1">{{ item.date }}</p>
          </div>
          <ElCheckbox v-model="item.complete" />
        </div>
      </ElScrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  interface TodoItem {
    username: string
    date: string
    complete: boolean
  }

  const props = withDefaults(
    defineProps<{
      todoList: TodoItem[]
    }>(),
    {
      todoList: () => []
    }
  )

  const list = reactive<TodoItem[]>([])

  watch(
    () => props.todoList,
    (value) => {
      list.splice(
        0,
        list.length,
        ...value.map((item) => ({
          username: item.username,
          date: item.date,
          complete: item.complete
        }))
      )
    },
    { immediate: true }
  )

  const pendingCount = computed(() => list.filter((item) => !item.complete).length)
</script>
