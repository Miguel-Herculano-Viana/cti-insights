import LandingView from '@/views/LandingView.vue'
import LoginView from '@/views/LoginView.vue'
import LogonView from '@/views/CadastroView.vue'
import UserPageView from '@/views/UserPageView.vue'
import DashboardView from '@/views/DashboardView.vue'
import UploadView from '@/views/UploadView.vue'
import UsuariosView from '@/views/UsuariosView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', name: 'Landing-Page', component: LandingView},
    {path:'/login', name:'Login', component: LoginView},
    {path:'/cadastro', name:'Cadastro', component: LogonView},
    {path:'/userPage', name:'UserPage', component: UserPageView, meta: { requiresAuth: true }},
    {path:'/dashboard', name:'Dashboard', component: DashboardView, meta: { requiresAuth: true }},
    {path:'/upload', name:'Upload', component: UploadView, meta: { requiresAuth: true }},
    {path:'/usuarios', name:'Usuarios', component: UsuariosView, meta: { requiresAuth: true }}
  ],
})

export default router
