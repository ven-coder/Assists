import { defineStore } from 'pinia'

// 定义日志项的类型
export interface LogContent {
    content: string;
}

export interface LogItem {
    time: string;
    images: string[];
    text: string;
}

export const useLogStore = defineStore('logs', {
    state: () => ({
        logs: [] as LogItem[]
    }),
    actions: {
        add({ images = [], text }: { images: string[], text: string }) {
            this.logs.unshift({
                time: new Date().toLocaleTimeString(),
                images,
                text
            })
        },
        clearLogs() {
            this.logs = []
        }
    }
}) 