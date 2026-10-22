import LandingView from '@/views/LandingView.vue'
import LoginView from '@/views/LoginView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', name: 'Landing-Page', component: LandingView},
    {path:'/Login', name:'Login', component: LoginView}
  ],
})

export default router
