import { defineStore } from 'pinia'

export const useNavigationStore = defineStore('navigation', {
    state: () => ({
        targetRoute: ''
    }),
    actions: {
        setTargetRoute(route: string) {
            this.targetRoute = route
        }
    }
}) 