<script setup lang="ts">
import { onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useLogStore } from '../stores/logStore'
import { Step, useStepStore } from 'assistsx'
import type { LogItem } from '../stores/logStore'

const router = useRouter()
const logStore = useLogStore()
const stepStore = useStepStore()
const originalTitle = document.title

// 计算返回按钮是否显示
const showBackButton = computed(() => {
  return stepStore.status === 'completed' || stepStore.status === 'error'
})

// 返回首页
const goBack = () => {
  router.back()
}

// 页面挂载时修改标题
onMounted(() => {
  document.title = '执行日志'
})

// 页面卸载时恢复原标题
onUnmounted(() => {
  document.title = originalTitle
})

const stopStep = () => {
  Step.stop()
  logStore.add({ images: [], text: '主动停止' })
}

// 暴露方法给父组件
defineExpose({
  addLog: logStore.add,
  clearLogs: logStore.clearLogs
})

// 处理图片点击
const handleImageClick = (imageUrl: string) => {
  // 可以在这里添加图片预览逻辑
  window.open(imageUrl, '_blank')
}

</script>

<template>
  <div class="log-page">
    <div class="log-header">
      <div class="header-buttons">
        <button class="icon-button" @click="goBack" v-if="showBackButton">
          <Icon icon="mdi:arrow-left" width="24" />
        </button>
        <button @click="stopStep" style="background-color: red;" v-if="!showBackButton">停止</button>
        <!-- <button @click="logStore.clearLogs">清空日志</button> -->
      </div>
    </div>
    <div class="log-content">
      <div v-for="(log, index) in logStore.logs" :key="index" class="log-item">
        <div class="log-time">{{ log.time }}</div>
        <div class="log-content-wrapper">
          <!-- 文本内容 -->
          <div class="log-text" v-if="log.text">{{ log.text }}</div>
          <!-- 图片网格 -->
          <div class="image-grid" v-if="log.images && log.images.length > 0">
            <img v-for="(image, imageIndex) in log.images" :key="imageIndex" :src="image" class="log-image"
              @click="() => handleImageClick(image)" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.log-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #ffffff;
  overflow: hidden;
}

.log-header {
  padding: 10px 0px 0px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.icon-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #ffffff;
}

.icon-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.log-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.log-item {
  font-family: monospace;
  padding-bottom: 5px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.log-time {
  color: #fff;
  font-size: 0.9em;
}

.log-content-wrapper {
  padding-left: 0px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  width: 100%;
}

.log-image {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.log-image:hover {
  transform: scale(1.02);
}

button {
  padding: 5px 10px;
  background-color: #646cff;
  color: white;
  border: none;
  border-radius: 4px;
  /* cursor: pointer; */
}

button:hover {
  background-color: #747bff;
}

.content-item {
  margin-bottom: 0px;
}

.content-item:last-child {
  margin-bottom: 0;
}
</style>