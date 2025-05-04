import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import LogPage from '../pages/LogPage.vue'
import UnfollowOfficialAccountPage from '../pages/UnfollowOfficialAccountPage.vue'

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomePage
        },
        {
            path: '/logs',
            name: 'logs',
            component: LogPage
        },
        {
            path: '/unfollow-official-account',
            name: 'UnfollowOfficialAccount',
            component: UnfollowOfficialAccountPage
        }
    ]
})

export default router 