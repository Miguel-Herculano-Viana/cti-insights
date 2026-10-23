import LandingView from '@/views/LandingView.vue'
import LoginView from '@/views/LoginView.vue'
import LogonView from '@/views/LogonView.vue'
import UserPageView from '@/views/UserPageView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', name: 'Landing-Page', component: LandingView},
    {path:'/Login', name:'Login', component: LoginView},
    {path:'/Logon', name:'Logon', component: LogonView},
    {path:'/UserPage', name:'UserPage', component: UserPageView}
  ],
})

export default router
