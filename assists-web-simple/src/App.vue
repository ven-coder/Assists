<script setup lang="ts">
import { AssistsX, NodeClassValue, sleep, Step } from 'assistsx'
import { onMounted, ref, watch } from 'vue'
import LogPage from './pages/LogPage.vue'
import { useRouter } from 'vue-router'
import { useNavigationStore } from './stores/navigationStore'

const router = useRouter()
const navigationStore = useNavigationStore()
// 页面挂载时修改标题
onMounted(() => {
  document.title = 'Assists Web示例'
})

watch(() => navigationStore.targetRoute, (newRoute) => {
  if (newRoute) {
    router.push(newRoute)
    navigationStore.setTargetRoute('')
  }
})
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
</style>
