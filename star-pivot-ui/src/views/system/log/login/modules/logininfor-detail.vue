<!-- 登录日志详情对话框 -->
<template>
  <ElDialog
    v-model="dialogVisible"
    title="登录日志详情"
    width="800px"
    :close-on-click-modal="false"
    @update:model-value="handleDialogChange"
  >
    <ElDescriptions :column="2" border v-if="logininfor">
      <ElDescriptionsItem label="日志ID">{{ logininfor.infoId }}</ElDescriptionsItem>
      <ElDescriptionsItem label="用户账号">{{ logininfor.userName }}</ElDescriptionsItem>
      <ElDescriptionsItem label="登录状态">
        <ElTag :type="logininfor.status === '0' ? 'success' : 'danger'">
          {{ logininfor.status === '0' ? '成功' : '失败' }}
        </ElTag>
      </ElDescriptionsItem>
      <ElDescriptionsItem label="登录时间">{{ logininfor.loginTime }}</ElDescriptionsItem>
      <ElDescriptionsItem label="登录IP">{{ logininfor.ipaddr }}</ElDescriptionsItem>
      <ElDescriptionsItem label="登录地点">
        {{ logininfor.loginLocation || '未知' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="浏览器类型">
        {{ logininfor.browser || '未知' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="操作系统">
        {{ logininfor.os || '未知' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="提示消息" :span="2">
        <ElText :type="logininfor.status === '0' ? 'success' : 'danger'">
          {{ logininfor.msg || '无' }}
        </ElText>
      </ElDescriptionsItem>
    </ElDescriptions>
  </ElDialog>
</template>

<script setup lang="ts">
import {ElDescriptions, ElDescriptionsItem, ElTag, ElText} from 'element-plus'
import type {LogininforListItem} from '@/types/api/logininfor'

interface Props {
    visible: boolean
    logininfor: LogininforListItem | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框显示状态
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  /**
   * 对话框状态变化处理
   */
  const handleDialogChange = (value: boolean) => {
    emit('update:visible', value)
  }
</script>

<style scoped lang="scss"></style>
