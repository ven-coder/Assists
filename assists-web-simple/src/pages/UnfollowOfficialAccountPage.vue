<template>
    <div class="unfollow-page">
        <div class="header">
            <button class="icon-button" @click="handleCancel">
                <Icon icon="mdi:arrow-left" width="24" />
            </button>
        </div>
        <div class="account-list">
            <el-checkbox-group v-model="selectedAccounts">
                <div v-for="account in officialAccounts" :key="account" class="account-item">
                    <el-checkbox :label="account">
                        {{ account }}
                    </el-checkbox>
                </div>
            </el-checkbox-group>
        </div>
        <div class="action-buttons">
            <el-button type="primary" @click="handleUnfollow" :disabled="!selectedAccounts.length">
                取消关注选中的公众号
            </el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElButton, ElCheckbox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { officialAccountList } from '../core/WechatCollectOfficialAccount'
import { start as startWechatUnfollowOfficialAccount } from '@/core/WechatUnfollowOfficialAccount'
const router = useRouter()
const officialAccounts = ref(officialAccountList)
const selectedAccounts = ref<string[]>([])
const originalTitle = document.title

// 页面挂载时修改标题
onMounted(() => {
    document.title = '选择要取消关注的公众号'
})
// 页面卸载时恢复原标题
onUnmounted(() => {
    document.title = originalTitle
})
const handleUnfollow = () => {
    startWechatUnfollowOfficialAccount(selectedAccounts.value)
}

const handleCancel = () => {
    router.back()
}
</script>

<style scoped>
.unfollow-page {
    padding: 20px;
    color: #ffffff;
    height: 100vh;
    display: flex;
    flex-direction: column;
}

.header {
    padding: 0 0 10px 0;
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

.account-list {
    margin: 0;
    flex: 1;
    overflow-y: auto;
    color: #ffffff;
}

.account-item {
    padding: 5px;
    color: #ffffff;
}

.account-item :deep(.el-checkbox__label) {
    color: #ffffff;
}

.account-item :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
    color: #ffffff;
}

.action-buttons {
    display: flex;
    gap: 10px;
    justify-content: center;
    margin-top: 20px;
}
</style>