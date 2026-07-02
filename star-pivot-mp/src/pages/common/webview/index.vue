<template>
  <web-view v-if="url" :src="url" />
  <view v-else class="hint">链接无效</view>
</template>

<script setup lang="ts">
import {onLoad} from '@dcloudio/uni-app'
import {ref} from 'vue'

const url = ref('')

onLoad((query) => {
  const raw = query?.url
  if (typeof raw === 'string' && raw) {
    url.value = decodeURIComponent(raw)
  }
})
</script>

<style scoped>
.hint {
  padding: 120rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}
</style>
