<!-- 操作日志基础字段 -->
<template>
  <div class="log-descriptions">
    <div class="log-descriptions-header">
      <span class="log-descriptions-title">操作日志详情</span>
      <div class="status-badge">
        <ElTag :type="operLog.status === 0 ? 'success' : 'danger'" size="small">
          {{ operLog.status === 0 ? '操作成功' : '操作异常' }}
        </ElTag>
      </div>
    </div>

    <div class="log-descriptions-body">
      <div class="log-item">
        <span class="log-label">日志ID</span>
        <span class="log-content id-value">{{ operLog.operId }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">模块标题</span>
        <span class="log-content title-value">{{ operLog.title }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">业务类型</span>
        <span class="log-content type-value">{{
          getOperBusinessTypeLabel(operLog.businessType)
        }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">操作状态</span>
        <span
          class="log-content"
          :class="['status-text', operLog.status === 0 ? 'status-success' : 'status-error']"
        >
          {{ operLog.status === 0 ? '正常' : '异常' }}
        </span>
      </div>
      <div class="log-item">
        <span class="log-label">操作人员</span>
        <span class="log-content user-value">{{ operLog.operName }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">部门名称</span>
        <span class="log-content dept-value">{{ operLog.deptName || '未知' }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">请求方式</span>
        <span class="log-content">
          <span :class="['method-badge', getHttpMethodClass(operLog.requestMethod)]">
            {{ operLog.requestMethod }}
          </span>
        </span>
      </div>
      <div class="log-item url-item">
        <span class="log-label">请求URL</span>
        <span class="log-content url-value">{{ operLog.operUrl }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">操作IP</span>
        <span class="log-content ip-value">{{ operLog.operIp }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">操作时间</span>
        <span class="log-content time-value">{{ operLog.operTime }}</span>
      </div>
      <div class="log-item">
        <span class="log-label">耗时</span>
        <span class="log-content">
          <span class="cost-value" :class="{ 'cost-high': (operLog.costTime || 0) > 1000 }">
            {{ operLog.costTime }}ms
          </span>
        </span>
      </div>
      <div class="log-item method-item">
        <span class="log-label">方法名称</span>
        <span class="log-content">
          <code class="method-code">{{ operLog.method }}</code>
        </span>
      </div>

      <OperLogJsonBlock label="请求参数" :content="operLog.operParam" />

      <OperLogJsonBlock
        v-if="operLog.jsonResult"
        label="返回结果"
        :content="operLog.jsonResult"
        result-style
      />

      <div v-if="operLog.errorMsg" class="log-item error-item">
        <span class="log-label">错误信息</span>
        <div class="log-content">
          <div class="error-wrapper">
            <div class="error-header">
              <span class="error-label">错误信息</span>
              <ElButton
                v-if="isErrorTooLong"
                size="small"
                text
                class="expand-btn"
                @click="errorExpanded = !errorExpanded"
              >
                <ElIcon>
                  <ArrowUpBold v-if="errorExpanded" />
                  <ArrowDownBold v-else />
                </ElIcon>
                {{ errorExpanded ? '收起' : '展开' }}
              </ElButton>
            </div>
            <ElAlert
              type="error"
              :title="''"
              :description="
                errorExpanded || !isErrorTooLong
                  ? operLog.errorMsg
                  : operLog.errorMsg.substring(0, 200) + '...'
              "
              show-icon
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import {ElAlert, ElButton, ElIcon, ElTag} from 'element-plus'
import {ArrowDownBold, ArrowUpBold} from '@element-plus/icons-vue'
import type {OperLogListItem} from '@/types/api/operlog'
import {getOperBusinessTypeLabel} from '../constants'
import {getHttpMethodClass} from './oper-log-utils'
import OperLogJsonBlock from './oper-log-json-block.vue'

const props = defineProps<{
    operLog: OperLogListItem
  }>()

  const errorExpanded = ref(false)
  const isErrorTooLong = computed(() => (props.operLog.errorMsg?.length ?? 0) > 200)
</script>

<style scoped lang="scss">
  @use './oper-log-detail.scss';
</style>
