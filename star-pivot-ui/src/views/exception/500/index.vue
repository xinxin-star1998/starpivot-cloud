<!-- 500页面 -->
<template>
  <ArtException
    :data="{
      title: '500',
      desc: t('exceptionPage.500'),
      btnText: t('exceptionPage.gohome'),
      imgUrl
    }"
  />
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n'
import imgUrl from '@imgs/svg/500.svg'

defineOptions({ name: 'Exception500' })

  const { t } = useI18n()
</script>
